package wbs.console.module;

import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.LoggedErrorsException;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ConsoleModuleSpecFactory
	implements ComponentFactory <ConsoleModuleSpec> {

	// singleton dependencies

	@SingletonDependency
	ConsoleModuleSpecReader consoleSpecReader;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String xmlResourceName;

	// implementation

	@Override
	public
	ConsoleModuleSpec makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			taskLogger.firstErrorFormat (
				"Error reading console module spec: %s",
				xmlResourceName);

			try {

				return consoleSpecReader.readClasspath (
					taskLogger,
					xmlResourceName);

			} catch (LoggedErrorsException loggedErrorsException) {

				throw taskLogger.makeException ();

			} catch (Exception exception) {

				throw new RuntimeException (
					stringFormat (
						"Error reading console module spec %s",
						xmlResourceName),
					exception);

			}

		}

	}

}
