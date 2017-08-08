declare variable $build := /wbs-build;

declare variable $mode external;

<web-app
	version="3.0"
	xmlns="http://java.sun.com/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="
		http://java.sun.com/xml/ns/javaee
		http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd
	">

	<display-name>{
		'WBS API'
	}</display-name>

	<!-- context params -->

	<context-param>

		<param-name>{
			'projects'
		}</param-name>

		<param-value>{ string-join (
			for $project in $build/projects/project
			return concat (
				'src/',
				replace ($project/@package, '\\.', '/'),
				'/',
				$project/@name,
				'-project.xml'
			),
		',' ) }</param-value>

	</context-param>

	<context-param>

		<param-name>{
			'beanDefinitionOutputPath'
		}</param-name>

		<param-value>{
			'work/api/bean-definitions'
		}</param-value>

	</context-param>

	<context-param>

		<param-name>{
			'layerNames'
		}</param-name>

		<param-value>{
			string-join ((
				'api',
				'config',
				'data',
				'entity',
				'hibernate',
				'logic',
				'model',
				'model-meta',
				'object',
				'process-api',
				'schema',
				'sql',
				'utils',
				'web'
			), ',')
		}</param-value>

	</context-param>

	<!-- listeners -->

	<listener>
		<listener-class>wbs.platform.servlet.WbsServletListener</listener-class>
	</listener>

	<!-- filters -->

	<filter>
		<filter-name>responseFilter</filter-name>
		<filter-class>wbs.framework.servlet.ComponentFilterProxy</filter-class>
	</filter>

	<!-- filter mappings -->

	<filter-mapping>
		<filter-name>responseFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- servlets -->

	<servlet>
		<display-name>default</display-name>
		<servlet-name>pathHandlerServlet</servlet-name>
		<servlet-class>wbs.framework.servlet.ComponentServletProxy</servlet-class>
	</servlet>

	<!-- servlet mappings -->

	<servlet-mapping>
		<servlet-name>pathHandlerServlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

</web-app>
