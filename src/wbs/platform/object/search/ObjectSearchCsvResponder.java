package wbs.platform.object.search;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.etc.ReflectionUtils.methodGetRequired;
import static wbs.utils.etc.ReflectionUtils.methodInvoke;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.camelToHyphen;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldSet;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.responder.ConsoleResponder;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("objectSearchCsvResponder")
public
class ObjectSearchCsvResponder <RecordType>
	extends ConsoleResponder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	ConsoleHelper <?> consoleHelper;

	@Getter @Setter
	List <FormFieldSet <RecordType>> formFieldSets;

	@Getter @Setter
	String resultsDaoMethodName;

	@Getter @Setter
	String sessionKey;

	// state

	FormatWriter formatWriter;

	Object searchObject;
	List <Long> objectIds;

	// implementation

	@Override
	protected
	void setup () {

		formatWriter =
			requestContext.formatWriter ();

	}

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		// set search object

		searchObject =
			requestContext.sessionRequired (
				sessionKey + "Fields");

		// get object ids

		List <Long> objectIdsTemp =
			genericCastUnchecked (
				requestContext.sessionRequired (
					sessionKey + "Results"));

		objectIds =
			objectIdsTemp;

	}


	@Override
	protected
	void setHtmlHeaders () {

		requestContext.setHeader (
			"Content-Type",
			"text/csv");

		requestContext.setHeader (
			"Content-Disposition",
			stringFormat (
				"attachment;filename=%s-search.csv",
				camelToHyphen (
					consoleHelper.objectName ())));

	}

	@Override
	protected
	void render (
			@NonNull TaskLogger parentTaskLogger) {

		Transaction transaction =
			database.currentTransaction ();

		// write headers

		formFieldLogic.outputCsvHeadings (
			formatWriter,
			formFieldSets);

		// iterate through objects

		int batchesSinceFlush = 0;

		for (
			List <Long> batch
				: Lists.partition (
					objectIds,
					64)
		) {

			List <Optional <RecordType>> objects;

			if (
				isNotNull (
					resultsDaoMethodName)
			) {

				Method method =
					methodGetRequired (
						consoleHelper.getClass (),
						resultsDaoMethodName,
						ImmutableList.<Class<?>> of (
							searchObject.getClass (),
							List.class));

				objects =
					genericCastUnchecked (
						methodInvoke (
							method,
							consoleHelper,
							searchObject,
							batch));

			} else {

				objects =
					genericCastUnchecked (
						consoleHelper.findMany (
							batch));

			}

			for (
				RecordType object
					: presentInstances (
						objects)
			) {

				// write object

				formFieldLogic.outputCsvRow (
					formatWriter,
					formFieldSets,
					object,
					ImmutableMap.of ());

			}

			// flush regularly

			if (++ batchesSinceFlush == 64) {

				transaction.flush ();

				batchesSinceFlush = 0;

			}

		}

	}

}
