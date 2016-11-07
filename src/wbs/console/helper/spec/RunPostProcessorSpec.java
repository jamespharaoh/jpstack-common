package wbs.console.helper.spec;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("run-post-processor")
@PrototypeComponent ("runPostProcessorSpec")
@ConsoleModuleData
public
class RunPostProcessorSpec {

	@DataAttribute
	String name;

}