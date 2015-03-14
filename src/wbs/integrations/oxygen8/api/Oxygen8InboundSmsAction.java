package wbs.integrations.oxygen8.api;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.Responder;
import wbs.integrations.oxygen8.model.Oxygen8ConfigRec;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8InboundLogRec;
import wbs.integrations.oxygen8.model.Oxygen8NetworkObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8NetworkRec;
import wbs.integrations.oxygen8.model.Oxygen8RouteInObjectHelper;
import wbs.integrations.oxygen8.model.Oxygen8RouteInRec;
import wbs.platform.api.mvc.ApiAction;
import wbs.platform.exception.logic.ExceptionLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.web.TextResponder;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;

@PrototypeComponent ("oxygen8InboundSmsAction")
public
class Oxygen8InboundSmsAction
	extends ApiAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogic exceptionLogic;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	Oxygen8InboundLogObjectHelper oxygen8InboundLogHelper;

	@Inject
	Oxygen8NetworkObjectHelper oxygen8NetworkHelper;

	@Inject
	Oxygen8RouteInObjectHelper oxygen8RouteInHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// prototype dependencies

	@Inject
	Provider<TextResponder> textResponderProvider;

	// state

	StringBuilder debugLog =
		new StringBuilder ();

	int routeId;

	String channel;
	String reference;
	String trigger;
	String shortcode;
	String msisdn;
	String content;
	Integer dataType;
	Long dateReceived;
	Integer campaignId;

	// implementation

	@Override
	protected
	Responder goApi () {

		try {

			logRequest ();

			processRequest ();

			updateDatabase ();

			return createResponse ();

		} catch (RuntimeException exception) {

			logFailure (
				exception);

			throw exception;

		} finally {

			storeLog ();

		}

	}

	void logRequest () {

		// output

		debugLog.append (
			stringFormat (
				"%s %s\n",
				requestContext.method (),
				requestContext.requestUri ()));

		// output headers

		for (
			Map.Entry<String,List<String>> headerEntry
				: requestContext.headerMap ().entrySet ()
		) {

			for (
				String headerValue
					: headerEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						headerEntry.getKey (),
						headerValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

		// output params

		for (
			Map.Entry<String,List<String>> parameterEntry
				: requestContext.parameterMap ().entrySet ()
		) {

			for (
				String parameterValue
					: parameterEntry.getValue ()
			) {

				debugLog.append (
					stringFormat (
						"%s = %s\n",
						parameterEntry.getKey (),
						parameterValue));

			}

		}

		debugLog.append (
			stringFormat (
				"\n"));

	}

	void logFailure (
			Throwable exception) {

		debugLog.append (
			stringFormat (
				"*** THREW EXCEPTION ***\n",
				"\n"));

		debugLog.append (
			stringFormat (
				"%s\n",
				exceptionLogic.throwableDump (
					exception)));

	}

	void storeLog () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		oxygen8InboundLogHelper.insert (
			new Oxygen8InboundLogRec ()

			.setTimestamp (
				transaction.now ())

			.setDetails (
				debugLog.toString ())

		);

		transaction.commit ();

	}

	void processRequest () {

		routeId =
			requestContext.requestInt ("routeId");

		channel =
			requestContext.parameter ("Channel");

		reference =
			requestContext.parameter ("Reference");

		trigger =
			requestContext.parameter ("Trigger");

		shortcode =
			requestContext.parameter ("Shortcode");

		msisdn =
			requestContext.parameter ("MSISDN");

		content =
			requestContext.parameter ("Content");

		dataType =
			Integer.parseInt (
				requestContext.parameter ("DataType"));

		if (dataType != 0) {

			throw new RuntimeException (
				stringFormat (
					"Don't know how to handle data type %s",
					dataType));

		}

		dateReceived =
			Long.parseLong (
				requestContext.parameter ("DateReceived"));

		campaignId =
			Integer.parseInt (
				requestContext.parameter ("CampaignID"));

	}

	void updateDatabase () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		RouteRec route =
			routeHelper.find (
				routeId);

		Oxygen8RouteInRec oxygen8RouteIn =
			oxygen8RouteInHelper.find (
				route.getId ());

		if (oxygen8RouteIn == null)
			throw new RuntimeException ();

		Oxygen8ConfigRec oxygen8Config =
			oxygen8RouteIn.getOxygen8Config ();

		Oxygen8NetworkRec oxygen8Network =
			oxygen8NetworkHelper.findByChannel (
				oxygen8Config,
				channel);

		if (oxygen8Network == null) {

			throw new RuntimeException (
				stringFormat (
					"Oxygen8 channel not recognised: %s",
					channel));

		}

		inboxLogic.inboxInsert (
			Optional.of (reference),
			textHelper.findOrCreate (content),
			msisdn,
			shortcode,
			route,
			Optional.of (oxygen8Network.getNetwork ()),
			Optional.of (new Instant (dateReceived)),
			Collections.<MediaRec>emptyList (),
			Optional.<String>absent (),
			Optional.<String>absent ());

		transaction.commit ();

	}

	Responder createResponse () {

		return textResponderProvider.get ()
			.text ("success");

	}

}
