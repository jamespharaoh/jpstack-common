package wbs.test.simulator.console;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.json.simple.JSONValue;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.test.simulator.model.SimulatorEventObjectHelper;
import wbs.test.simulator.model.SimulatorEventRec;
import wbs.web.responder.JsonResponder;
import wbs.web.responder.WebResponder;

@PrototypeComponent ("simulatorSessionPollAction")
public
class SimulatorSessionPollAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SimulatorEventObjectHelper simulatorEventHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <JsonResponder> jsonResponderProvider;

	// implementation

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			Long lastId =
				parseIntegerRequired (
					requestContext.formRequired (
						"last"));

			Long limit =
				parseIntegerRequired (
					requestContext.formRequired (
						"limit"));

			List <SimulatorEventRec> events =
				simulatorEventHelper.findAfterLimit (
					transaction,
					lastId,
					limit);

			// create response

			Map <String, Object> responseObject =
				new LinkedHashMap<> ();

			List<Object> eventResponses =
				new ArrayList<Object> ();

			for (
				SimulatorEventRec event
					: events
			) {

				eventResponses.add (
					ImmutableMap.<String,Object>builder ()

					.put (
						"id",
						event.getId ())

					.put (
						"date",
						userConsoleLogic.dateStringShort (
							transaction,
							event.getTimestamp ()))

					.put (
						"time",
						userConsoleLogic.timeString (
							transaction,
							event.getTimestamp ()))

					.put (
						"type",
						event.getType ())

					.put (
						"data",
						JSONValue.parse (
							event.getData ()))

					.build ()

				);

			}

			responseObject.put (
				"events",
				eventResponses);

			// return it

			return jsonResponderProvider.provide (
				transaction,
				jsonProvider ->
					jsonProvider

				.value (
					responseObject)

			);

		}

	}

}
