package wbs.sms.route.router.model;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelMetaBuilderHandler;
import wbs.framework.entity.model.ModelMetaSpec;

@PrototypeComponent ("routerTypesBuilder")
@ModelMetaBuilderHandler
public
class RouterTypesBuilder {

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	RouterTypesSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			Builder builder) {

		builder.descend (
			parent,
			spec.routerTypes (),
			model);

	}

}