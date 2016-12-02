package wbs.smsapps.forwarder.daemon;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrDefault;
import static wbs.utils.etc.LogicUtils.parseBooleanYesNoRequired;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;
import wbs.platform.daemon.QueueBuffer;

import wbs.smsapps.forwarder.model.ForwarderMessageInObjectHelper;
import wbs.smsapps.forwarder.model.ForwarderMessageInRec;

@SingletonComponent ("forwarderDaemon")
public
class ForwarderDaemon
	extends AbstractDaemonService {

	// constants

	public final static
	int bufferSize = 100;

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ForwarderMessageInObjectHelper forwarderMessageInHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	QueueBuffer<Long,Long> buffer =
		new QueueBuffer<> (
			bufferSize);

	private
	class MainThread
		implements Runnable {

		public
		boolean doQuery () {

			Set<Long> activeIds =
				buffer.getKeys ();

			List <ForwarderMessageInRec> forwarderMessageIns;

			try (

				Transaction transaction =
					database.beginReadOnly (
						"ForwarderDaemon.MainThread.doQuery ()",
						this);

			) {

				// get the list

				forwarderMessageIns =
					forwarderMessageInHelper.findNextLimit (
						transaction.now (),
						buffer.getFullSize ());

				// initialise any proxies

				for (
					ForwarderMessageInRec forwarderMessageIn
						: forwarderMessageIns
				) {

					forwarderMessageIn
						.getMessage ()
						.getText ()
						.getText ();

					forwarderMessageIn
						.getForwarder ()
						.getUrl ();

				}

				transaction.close ();

				int numRetrieved = 0;

				for (
					ForwarderMessageInRec forwarderMessageIn
						: forwarderMessageIns
				) {

					numRetrieved ++;

					if (
						activeIds.contains (
							forwarderMessageIn.getId ())
					) {
						continue;
					}

					buffer.add (
						forwarderMessageIn.getId (),
						forwarderMessageIn.getId ());

				}

				return numRetrieved == buffer.getFullSize ();

			}

		}

		@Override
		public
		void run () {

			while (true) {

				boolean moreMessages = doQuery();

				try {
					if (moreMessages)
						buffer.waitNotFull();
					else
						Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}

			}

		}

	}

	private static
	Pattern paramsPattern =
		Pattern.compile (
			"\\{(message|numfrom|numto|in_id)\\}");

	private static
	Pattern successPattern =
		Pattern.compile (
			"\\s*ok\\b",
			Pattern.CASE_INSENSITIVE);

	private
	class WorkerThread
		implements Runnable {

		private
		String getParams (
				ForwarderMessageInRec forwarderMessageIn) {

			try {

				StringBuilder stringBuilder =
					new StringBuilder ();

				Matcher matcher =
					paramsPattern.matcher (
						forwarderMessageIn
							.getForwarder ()
							.getUrlParams ());

				int i = 0;

				while (matcher.find ()) {

					stringBuilder.append (
						forwarderMessageIn
							.getForwarder ()
							.getUrlParams ()
							.substring (
								i,
								matcher.start ()));

					String paramName =
						matcher.group (1);

					if (paramName.equals("message")) {

						stringBuilder.append (
							URLEncoder.encode (
								forwarderMessageIn
									.getMessage ()
									.getText ()
									.getText (),
								"utf-8"));

					} else if (
						paramName.equals ("num_from")
						|| paramName.equals ("numfrom")
					) {

						stringBuilder.append (
							URLEncoder.encode (
								forwarderMessageIn.getMessage ().getNumFrom (),
								"utf-8"));

					} else if (
						paramName.equals ("num_to")
						|| paramName.equals ("numto")
					) {

						stringBuilder.append (
							URLEncoder.encode (
								forwarderMessageIn
									.getMessage ()
									.getNumTo (),
								"utf-8"));

					} else if (paramName.equals("in_id")) {

						stringBuilder.append (
							URLEncoder.encode (
								Long.toString (
									forwarderMessageIn.getId ()),
								"utf-8"));

					}

					i = matcher.end ();

				}

				stringBuilder.append (
					forwarderMessageIn
						.getForwarder ()
						.getUrlParams ()
						.substring (i));

				return stringBuilder.toString();

			} catch (UnsupportedEncodingException exception) {

				throw new RuntimeException (
					"Error encoding request params",
					exception);

			}

		}

		private
		HttpURLConnection openPost (
				ForwarderMessageInRec forwarderMessageIn)
			throws IOException {

			String params =
				getParams (forwarderMessageIn);

			// create and open URL connection

			URL urlObj =
				new URL (
					forwarderMessageIn
						.getForwarder ()
						.getUrl ());

			HttpURLConnection urlConn =
				(HttpURLConnection)
				urlObj.openConnection ();

			urlConn.setDoInput (
				true);

			urlConn.setDoOutput (
				true);

			urlConn.setAllowUserInteraction (
				false);

			urlConn.setRequestMethod (
				"POST");

			urlConn.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			urlConn.setRequestProperty (
				"Content-Type",
				"application/x-www-form-urlencoded");

			urlConn.setRequestProperty (
				"Content-Length",
				Integer.toString (
					params.length ()));

			// send request

			Writer out =
				new OutputStreamWriter (
					urlConn.getOutputStream (),
					"iso-8859-1");

			out.write (params);

			out.flush ();

			return urlConn;

		}

		private
		HttpURLConnection openGet (
				ForwarderMessageInRec forwarderMessageIn)
			throws IOException {

			URL urlObj =
				new URL (
					stringFormat (
						"%s",
						forwarderMessageIn.getForwarder ().getUrl (),
						"?%s",
						getParams (forwarderMessageIn)));

			HttpURLConnection urlConnection =
				(HttpURLConnection)
				urlObj.openConnection ();

			urlConnection.setDoInput (
				true);

			urlConnection.setAllowUserInteraction (
				false);

			urlConnection.setRequestMethod (
				"GET");

			urlConnection.setRequestProperty (
				"User-Agent",
				wbsConfig.httpUserAgent ());

			return urlConnection;

		}

		private
		boolean doSend (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull ForwarderMessageInRec forwarderMessageIn) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"WorkerThread.doSend");

			if (

				stringIsEmpty (
					forwarderMessageIn.getForwarder ().getUrl ())

				|| stringIsEmpty (
					forwarderMessageIn.getForwarder ().getUrlParams ())

			) {

				return false;

			}

			try {

				HttpURLConnection urlConnection =
					forwarderMessageIn.getForwarder ().getUrlPost ()
						? openPost (forwarderMessageIn)
						: openGet (forwarderMessageIn);

				Reader in =
					new InputStreamReader (
						urlConnection.getInputStream (),
						"utf-8");

				StringBuffer responseBuffer =
					new StringBuffer ();

				int numread;
				char[] buffer = new char[1024];
				while ((numread = in.read(buffer, 0, 1024)) > 0)
					responseBuffer.append(buffer, 0, numread);
				String response = responseBuffer.toString();
				return successPattern.matcher(response).matches();

			} catch (IOException exception) {

				taskLogger.warningFormat (
					"IO exception forwarding message: %s",
					exception.getMessage ());

				return false;

			}

		}

		private
		void doMessage (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long forwarderMessageInId) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"WorkerThread.doMessage");

			@Cleanup
			Transaction checkTransaction =
				database.beginReadWrite (
					stringFormat (
						"%s.%s.%s (%s)",
						"ForwarderDaemon",
						"WorkerThread",
						"doMessage",
						stringFormat (
							"forwarderMessageInId = %s",
							integerToDecimalString (
								forwarderMessageInId))),
					this);

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInHelper.findRequired (
					forwarderMessageInId);

			// check if we should cancel it

			long timeout =
				forwarderMessageIn
					.getForwarder ()
					.getInboundTimeoutSecs ();

			if (

				timeout > 0

				&& earlierThan (
					forwarderMessageIn.getCreatedTime ().plus (
						Duration.standardSeconds (
							timeout)),
					checkTransaction.now ())
			) {

				forwarderMessageIn =
					forwarderMessageInHelper.findRequired (
						forwarderMessageIn.getId ());

				forwarderMessageIn

					.setCancelledTime (
						checkTransaction.now ());

				checkTransaction.commit ();

				return;

			} else {

				checkTransaction.commit ();

			}

			// send it

			boolean success =
				doSend (
					taskLogger,
					forwarderMessageIn);

			@Cleanup
			Transaction resultTransaction =
				database.beginReadWrite (
					"ForwarderDaemon.WorkerThread.doMessage",
					this);

			doResult (
				forwarderMessageIn.getId (),
				success);

			resultTransaction.commit ();

		}

		private
		void doResult (
				@NonNull Long forwarderMessageInId,
				@NonNull Boolean success) {

			Transaction transaction =
				database.currentTransaction ();

			ForwarderMessageInRec forwarderMessageIn =
				forwarderMessageInHelper.findRequired (
					forwarderMessageInId);

			if (! forwarderMessageIn.getPending ())
				return;

			if (success) {

				forwarderMessageIn

					.setPending (
						false)

					.setSendQueue (
						false)

					.setProcessedTime (
						transaction.now ())

					.setRetryTime (
						null);

			} else {

				Calendar calendar =
					Calendar.getInstance ();

				calendar.add (Calendar.MINUTE, 1);

				forwarderMessageIn

					.setRetryTime (
						transaction.now ().plus (
							Duration.standardMinutes (1)));

			}

		}

		@Override
		public
		void run () {

			while (true) {

				Long forwarderMessageInId;

				try {

					forwarderMessageInId =
						buffer.next ();

				} catch (InterruptedException exception) {

					return;

				}

				TaskLogger taskLogger =
					logContext.createTaskLogger (
						"WorkerThread.run");

				doMessage (
					taskLogger,
					forwarderMessageInId);

				buffer.remove (
					forwarderMessageInId);

			}

		}

	}

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	@Override
	protected
	boolean checkEnabled () {

		return parseBooleanYesNoRequired (
			mapItemForKeyOrDefault (
				wbsConfig.runtimeSettings (),
				"forwarder-daemon.enable",
				"yes"));

	}

	@Override
	protected
	void createThreads () {

		// main thread

		Thread mainThread;

		mainThread =
			threadManager.makeThread (
				new MainThread (),
				"Forwarder-Main");

		mainThread.setName (
			"ForwarderDaemon");

		mainThread.start ();

		registerThread (
			mainThread);

		// worker threads

		for (int i = 0; i < 1; i++) {

			Thread workerThread =
				threadManager.makeThread (
					new WorkerThread (),
					"Forwarder-" + i);

			workerThread.setName (
				"ForwarderDaemon worker " + i);

			workerThread.start ();

			registerThread (
				workerThread);

		}

	}

}
