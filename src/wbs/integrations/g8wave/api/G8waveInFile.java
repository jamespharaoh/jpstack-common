package wbs.integrations.g8wave.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.RequestContext;
import wbs.framework.web.WebFile;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("g8waveInFile")
public
class G8waveInFile
	implements WebFile {

	@Inject
	Database database;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	RequestContext requestContext;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	@Override
	public
	void doGet ()
		throws
			ServletException,
			IOException {

		doPost ();

	}

	@Override
	public
	void doPost ()
		throws
			ServletException,
			IOException {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"G8waveInFile.doPost ()",
				this);

		// get request stuff

		int routeId =
			requestContext.requestIntRequired (
				"route_id");

		// get params in local variables

		String numFromParam =
			requestContext.parameterOrNull ("telno");

		String numToParam =
			requestContext.parameterOrNull ("shortcode");

		String networkParam =
			requestContext.parameterOrNull ("network");

		String messageParam =
			requestContext.parameterOrNull ("message");

		if (
			numFromParam == null
			|| numToParam == null
			|| messageParam == null
		) {
			throw new ServletException ("Parameter not supplied");
		}

		Integer networkId = null;

		if (networkParam != null) {

			if (networkParam.equals("ORANGE"))
				networkId = 1;

			else if (networkParam.equals("VODA"))
				networkId = 2;

			else if (networkParam.equals("TMOB"))
				networkId = 3;

			else if (networkParam.equals("O2"))
				networkId = 4;

			else if (networkParam.equals("THREE"))
				networkId = 6;

			else
				throw new ServletException (
					"Unknown network: " + networkParam);
		}

		// load the stuff

		RouteRec route =
			routeHelper.findRequired (
				routeId);

		NetworkRec network =
			networkId == null
				? null
				: networkHelper.findRequired (
					networkId);

		// insert the message

		smsInboxLogic.inboxInsert (
			Optional.<String>absent (),
			textHelper.findOrCreate (messageParam),
			numFromParam,
			numToParam,
			route,
			Optional.fromNullable (network),
			Optional.<Instant>absent (),
			Collections.<MediaRec>emptyList (),
			Optional.<String>absent (),
			Optional.<String>absent ());

		transaction.commit ();

		PrintWriter out =
			requestContext.writer ();

		out.println ("OK");

	}

	@Override
	public
	void doOptions ()
		throws
			ServletException,
			IOException {

	}

}
