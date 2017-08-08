declare variable $project external;

declare variable $server-port :=
	if ($project eq "wbs-hades-dev") then "8180"
	else if ($project eq "shopping-nation-dev") then "7180"
	else "ERROR";

declare variable $http-port :=
	if ($project eq "wbs-hades-dev") then "8080"
	else if ($project eq "shopping-nation-dev") then "7080"
	else "ERROR";

<Server port="{ $server-port }" shutdown="SHUTDOWN">

	<Listener
		className="org.apache.catalina.core.AprLifecycleListener"
		SSLEngine="on"/>

	<GlobalNamingResources>

		<Resource
			name="UserDatabase"
			auth="Container"
			type="org.apache.catalina.UserDatabase"
			description="User database that can be updated and saved"
			factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
			pathname="conf/tomcat-users.xml"/>

	</GlobalNamingResources>

	<Service name="console">

		<Executor
			name="console-executor"
			namePrefix="console-exec-"
			maxThreads="150"
			minSpareThreads="4"/>

		<Connector
			port="{ $http-port }"
			protocol="HTTP/1.1"
			connectionTimeout="20000"/>

		<Engine
			name="console-engine"
			defaultHost="localhost">

			<Realm
				className="org.apache.catalina.realm.UserDatabaseRealm"
				resourceName="UserDatabase"/>

			<Host
				name="localhost"
				appBase="apps/console"
				unpackWARs="false"
				autoDeploy="false">

				<Context
					docBase="ROOT"
					path=""
					reloadable="true"/>

			</Host>

		</Engine>

	</Service>

</Server>
