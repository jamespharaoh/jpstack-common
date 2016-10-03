package wbs.framework.schema.tool;

import javax.inject.Provider;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
import wbs.framework.component.config.WbsSpecialConfig;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.SimpleExceptionLogger;

@SingletonComponent ("schemaToolComponents")
public
class SchemaToolComponents {

	// prototype dependencies

	@UninitializedDependency
	Provider <SimpleExceptionLogger> simpleExceptionLoggerProvider;

	// components

	@SingletonComponent ("exceptionLogger")
	public
	ExceptionLogger exceptionLogger () {

		return simpleExceptionLoggerProvider.get ();

	}

	@SingletonComponent ("wbsSpecialConfig")
	public
	WbsSpecialConfig wbsSpecialConfig () {

		return new WbsSpecialConfig ()

			.assumeNegativeCache (
				true);

	}

}