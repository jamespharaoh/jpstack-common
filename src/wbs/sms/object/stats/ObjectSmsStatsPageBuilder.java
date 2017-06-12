package wbs.sms.object.stats;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.stringFormat;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.context.ConsoleContextBuilderContainer;
import wbs.console.context.ResolvedConsoleContextExtensionPoint;
import wbs.console.helper.core.ConsoleHelper;
import wbs.console.module.ConsoleMetaManager;
import wbs.console.module.ConsoleModuleBuilderComponent;
import wbs.console.module.ConsoleModuleImplementation;
import wbs.console.part.PagePartFactory;
import wbs.console.responder.ConsoleFile;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.TabContextResponder;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("objectSmsStatsPageBuilder")
public
class ObjectSmsStatsPageBuilder <
	ObjectType extends Record <ObjectType>
> implements ConsoleModuleBuilderComponent {

	// singleton dependencies

	@SingletonDependency
	ConsoleMetaManager consoleMetaManager;

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <ConsoleFile> consoleFileProvider;

	@PrototypeDependency
	Provider <ConsoleContextTab> contextTabProvider;

	@PrototypeDependency
	Provider <TabContextResponder> tabContextResponderProvider;

	@PrototypeDependency
	Provider <ObjectStatsPartFactory> objectStatsPartFactoryProvider;

	// builder

	@BuilderParent
	ConsoleContextBuilderContainer <ObjectType> container;

	@BuilderSource
	ObjectSmsStatsPageSpec spec;

	@BuilderTarget
	ConsoleModuleImplementation consoleModule;

	// state

	ConsoleHelper <ObjectType> consoleHelper;
	String privKey;

	Provider <WebResponder> responderProvider;

	// build

	@Override
	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder <TaskLogger> builder) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildConsoleModule");

		) {

			setDefaults ();

			buildResponder ();

			for (
				ResolvedConsoleContextExtensionPoint resolvedExtensionPoint
					: consoleMetaManager.resolveExtensionPoint (
						container.extensionPointName ())
			) {

				buildContextTab (
					taskLogger,
					resolvedExtensionPoint);

				buildContextFile (
					taskLogger,
					resolvedExtensionPoint);

			}

		}

	}

	void buildContextTab (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextTab");

		) {

			consoleModule.addContextTab (
				taskLogger,
				"end",
				contextTabProvider.get ()

					.name (
						consoleHelper.objectName () + ".stats")

					.defaultLabel (
						"Stats")

					.localFile (
						consoleHelper.objectName () + ".stats")

					.privKeys (
						privKey),

				extensionPoint.contextTypeNames ());

		}

	}

	void buildContextFile (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ResolvedConsoleContextExtensionPoint extensionPoint) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"buildContextFile");

		) {

			consoleModule.addContextFile (
				consoleHelper.objectName () + ".stats",
				consoleFileProvider.get ()

					.getResponderProvider (
						responderProvider)

					.privKeys (
						singletonList (
							privKey)),

				extensionPoint.contextTypeNames ()
			);

		}

	}

	void buildResponder () {

		PagePartFactory partFactory =
			objectStatsPartFactoryProvider.get ()

			.localName (
				"/" + consoleHelper.objectName () + ".stats")

			.objectLookup (
				consoleHelper);

		responderProvider =
			() -> tabContextResponderProvider.get ()

			.tab (
				consoleHelper.objectName () + ".stats")

			.title (
				capitalise (
					consoleHelper.friendlyName () + " stats"))

			.pagePartFactory (
				partFactory)

		;

	}

	void setDefaults () {

		consoleHelper =
			container.consoleHelper ();

		privKey =
			ifNull (
				spec.privKey (),
				stringFormat (
					"%s.stats",
					consoleHelper.objectName ()));

	}

}
