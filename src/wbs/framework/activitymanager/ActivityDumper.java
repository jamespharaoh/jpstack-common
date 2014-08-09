package wbs.framework.activitymanager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("activityDumper")
public
class ActivityDumper {

	// dependencies

	@Inject
	ActivityManagerImplementation activityManager;

	// implementation

	@PostConstruct
	public
	void init () {

		Runnable runnable =
			new Runnable () {

			@Override
			public
			void run () {
				ActivityDumper.this.run ();
			}

		};

		Thread thread =
			new Thread (
				runnable,
				"ActivityDumper");

		thread.start ();

	}

	void run () {

		try {

			runReal ();

		} catch (InterruptedException exception) {

			return;

		} catch (Exception exception) {

			System.out.println ("ERROR");

		}

	}

	void runReal ()
		throws InterruptedException {

		Thread.sleep (
			5000);

		for (;;) {

			runOnce ();

			Thread.sleep (
				5000);

		}

	}

	void runOnce () {

		activityManager.dump ();

	}

}