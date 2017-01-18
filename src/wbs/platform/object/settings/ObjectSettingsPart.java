package wbs.platform.object.settings;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostActionEncoding;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FieldsProvider;
import wbs.console.forms.FormFieldLogic;
import wbs.console.forms.FormFieldLogic.UpdateResultSet;
import wbs.console.forms.FormFieldSet;
import wbs.console.forms.FormType;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.RootObjectHelper;

@Accessors (fluent = true)
@PrototypeComponent ("objectSettingsPart")
public
class ObjectSettingsPart <
	ObjectType extends Record <ObjectType>,
	ParentType extends Record <ParentType>
>
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	FormFieldLogic formFieldLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	RootObjectHelper rootHelper;

	// properties

	@Getter @Setter
	ObjectLookup <ObjectType> objectLookup;

	@Getter @Setter
	ConsoleHelper <ObjectType> consoleHelper;

	@Getter @Setter
	String editPrivKey;

	@Getter @Setter
	String localName;

	@Getter @Setter
	FormFieldSet <ObjectType> formFieldSet;

	@Getter @Setter
	String removeLocalName;

	@Getter @Setter
	FieldsProvider <ObjectType, ParentType> formFieldsProvider;

	// state

	Optional <UpdateResultSet> updateResultSet;
	ObjectType object;
	ParentType parent;
	boolean canEdit;

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new LinkedHashSet<ScriptRef> ();

		scriptRefs.addAll (
			formFieldSet.scriptRefs ());

		return scriptRefs;

	}

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		updateResultSet =
			optionalCast (
				UpdateResultSet.class,
				requestContext.request (
					"objectSettingsUpdateResultSet"));

		object =
			objectLookup.lookupObject (
				requestContext.consoleContextStuffRequired ());

		canEdit = (

			editPrivKey != null

			&& requestContext.canContext (
				editPrivKey)

		);

		if (formFieldsProvider != null) {

			prepareParent ();

			prepareFieldSet (
				taskLogger);

		}

	}

	void prepareParent () {

		ConsoleHelper <ParentType> parentHelper =
			objectManager.findConsoleHelperRequired (
				consoleHelper.parentClass ());

		if (parentHelper.isRoot ()) {

			parent =
				parentHelper.findRequired (
					0l);

			return;

		}

		Optional <Long> parentIdOptional =
			requestContext.stuffInteger (
				parentHelper.idKey ());

		if (
			optionalIsPresent (
				parentIdOptional)
		) {

			// use specific parent

			parent =
				parentHelper.findRequired (
					optionalGetRequired (
						parentIdOptional));

			return;

		}

	}

	void prepareFieldSet (
			@NonNull TaskLogger parentTaskLogger) {

		formFieldSet =
			formFieldsProvider.getFieldsForObject (
				parentTaskLogger,
				object);

	}

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		if (canEdit) {

			String enctype =
				"application/x-www-form-urlencoded";

			try {

				if (formFieldSet.fileUpload ()) {

					enctype =
						"multipart/form-data";

				}

			} catch (Exception exception) {

				enctype =
					"application/x-www-form-urlencoded";

			}

			htmlFormOpenPostActionEncoding (
				requestContext.resolveLocalUrl (
					localName),
				enctype);

		}

		htmlTableOpenDetails ();

		formFieldLogic.outputFormRows (
			taskLogger,
			requestContext,
			formatWriter,
			formFieldSet,
			updateResultSet,
			object,
			ImmutableMap.of (),
			FormType.update,
			"settings");

		htmlTableClose ();

		if (canEdit) {

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose ();

			htmlFormClose ();

			if (removeLocalName != null) {

				htmlHeadingTwoWrite (
					"Remove");

				htmlFormOpenPostAction (
					requestContext.resolveLocalUrl (
						removeLocalName));

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" value=\"remove\"",
					">");

				htmlFormClose ();

			}

		}

	}

}
