declare variable $build := /wbs-build;

declare variable $mode external;
declare variable $config external;

<web-app
	version="3.0"
	xmlns="http://java.sun.com/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="
		http://java.sun.com/xml/ns/javaee
		http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd
	">

	<display-name>{
		'WBS console'
	}</display-name>

	<!-- context init params -->

	<context-param>

		<param-name>{
			'componentDefinitionOutputPath'
		}</param-name>

		<param-value>{
			'work/console/component-definitions'
		}</param-value>

	</context-param>

	<context-param>

		<param-name>{
			'layerNames'
		}</param-name>

		<param-value>{
			string-join ((
				'utils',
				'config',
				'data',
				'entity',
				'schema',
				'sql',
				'model',
				'hibernate',
				'model-meta',
				'object',
				'logic',
				'web',
				'console',
				if ($mode = 'test') then (
					'daemon'
				) else ()
			), ',')
		}</param-value>

	</context-param>

	<!-- filters -->

	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>wbs.framework.servlet.SetCharacterEncodingFilter</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
	</filter>

	<filter>
		<filter-name>responseFilter</filter-name>
		<filter-class>wbs.framework.servlet.ComponentFilterProxy</filter-class>
	</filter>

	<filter>
		<filter-name>coreAuthFilter</filter-name>
		<filter-class>wbs.framework.servlet.ComponentFilterProxy</filter-class>
	</filter>

	<!-- filter mappings -->

	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<filter-mapping>
		<filter-name>responseFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<filter-mapping>
		<filter-name>coreAuthFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- listeners -->

	<listener>

		<listener-class>{
			'wbs.platform.servlet.WbsServletListener'
		}</listener-class>

	</listener>

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

	{ for $extension in (
		'css',
		'js',
		'gif',
		'txt',
		'png',
		'ico',
		'swf',
		'yml'
	) return (

		<servlet-mapping>
			<servlet-name>default</servlet-name>
			<url-pattern>*.{ $extension }</url-pattern>
		</servlet-mapping>

	) }

	<mime-mapping>
		<extension>ico</extension>
		<mime-type>image/vnd.microsoft.icon</mime-type>
	</mime-mapping>

	<!-- welcome file -->

	<welcome-file-list>
		<welcome-file>HOME</welcome-file>
	</welcome-file-list>

</web-app>
