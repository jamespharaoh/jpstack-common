package wbs.framework.entity.generate.fields;

import static wbs.framework.utils.etc.StringUtils.capitalise;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.scaffold.PluginManager;
import wbs.framework.application.scaffold.PluginModelSpec;
import wbs.framework.application.scaffold.PluginSpec;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.codegen.JavaPropertyWriter;
import wbs.framework.entity.generate.ModelWriter;
import wbs.framework.entity.meta.TypeFieldSpec;
import wbs.framework.utils.formatwriter.FormatWriter;

@PrototypeComponent ("typeFieldWriter")
@ModelWriter
public
class TypeFieldWriter {

	// dependencies

	@Inject
	PluginManager pluginManager;

	// builder

	@BuilderParent
	ModelFieldWriterContext context;

	@BuilderSource
	TypeFieldSpec spec;

	@BuilderTarget
	FormatWriter javaWriter;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		String fieldTypeName =
			ifNull (
				spec.typeName (),
				stringFormat (
					"%sType",
					context.modelMeta ().name ()));

		String fieldName =
			ifNull (
				spec.name (),
				fieldTypeName);

		PluginModelSpec fieldTypePluginModel =
			pluginManager.pluginModelsByName ().get (
				fieldTypeName);

		PluginSpec fieldTypePlugin =
			fieldTypePluginModel.plugin ();

		String fullFieldTypeName =
			stringFormat (
				"%s.model.%sRec",
				fieldTypePlugin.packageName (),
				capitalise (
					fieldTypeName));

		// write field

		JavaPropertyWriter propertyWriter =
			new JavaPropertyWriter ()

			.thisClassNameFormat (
				"%s",
				context.recordClassName ())

			.typeNameFormat (
				"%s",
				fullFieldTypeName)

			.propertyNameFormat (
				"%s",
				fieldName);

		propertyWriter.write (
			javaWriter,
			"\t");

	}

}
