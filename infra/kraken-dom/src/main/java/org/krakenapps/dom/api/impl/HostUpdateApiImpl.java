package org.krakenapps.dom.api.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.HostUpdateApi;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.windows.WindowsHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-host-update-api")
@Provides
public class HostUpdateApiImpl implements HostUpdateApi, Runnable {
	private final Logger logger = LoggerFactory.getLogger(HostUpdateApiImpl.class.getName());

	@Requires
	private HostApi hostApi;

	private ConcurrentLinkedQueue<Host> queue = new ConcurrentLinkedQueue<Host>();

	private Thread t;
	private volatile boolean doStop;

	@Validate
	public void start() {
		doStop = false;
		t = new Thread(this, "DOM Host Updater");
		t.start();
	}

	@Invalidate
	public void stop() {
		doStop = true;
		t.interrupt();
	}

	/**
	 * batch update using
	 */
	@Override
	public void run() {
		try {
			logger.info("kraken dom: starting host updater thread");
			while (!doStop) {
				try {
					runOnce();
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					logger.debug("kraken dom: host batch update interrupted");
				}
			}
		} catch (Throwable e) {
			logger.error("kraken dom: host batch update error", e);
		} finally {
			logger.info("kraken dom: host updater thread stopped");
		}
	}

	@Override
	public void update(Host host) {
		Object windows = host.getData().get("windows");
		if (windows != null) {
			WindowsHost whost = PrimitiveConverter.parse(WindowsHost.class, windows);
			validateHardeningKeys(whost);
		}

		queue.add(host);
	}

	private void validateHardeningKeys(WindowsHost whost) {
		final String[] validHardeningKeys = new String[] { "UseFirewall", "UseLoginPassword", "UseScreenSaver",
				"UseScreenSaverPassword", "UseShareFolderPassword", "UseManagementShareFolder", "UseWindowsUpdate",
				"UseAutoLogin", "UseGuestAccount", "MinimumPasswordLength", "PasswordExpiry", "ScreenSaverIdleInterval" };

		for (String key : whost.getHardenings().keySet()) {
			boolean isFound = false;
			for (String hardeningKey : validHardeningKeys) {
				if (hardeningKey.equals(key)) {
					isFound = true;
					break;
				}
			}
			if (!isFound) {
				logger.error("kraken dom : invalidate Hardening key");
				throw new IllegalArgumentException("invalidate Hardening key");
			}
		}
	}

	private void runOnce() {
		Map<String, Host> hosts = new HashMap<String, Host>();
		Map<String, Host> recordedHosts = new HashMap<String, Host>();
		Map<String, Host> addHosts = new HashMap<String, Host>();
		Map<String, Host> updateHosts = new HashMap<String, Host>();

		AtomicInteger aleadyExist = new AtomicInteger(0);
		while (true) {
			Host host = queue.poll();
			if (host == null)
				break;
			if (hosts.get(host.getGuid()) != null)
				aleadyExist.incrementAndGet();
			hosts.put(host.getGuid(), host);
		}

		long begin = new Date().getTime();

		Collection<Host> existedHost = hostApi.findHosts("localhost", hosts.keySet());

		for (Host h : existedHost)
			recordedHosts.put(h.getGuid(), h);

		for (String guid : hosts.keySet()) {
			Host h = recordedHosts.get(guid);
			if (h == null)
				addHosts.put(guid, hosts.get(guid));
			else
				updateHosts.put(guid, h);
		}

		long lockBegin = new Date().getTime();
		if (addHosts.size() > 0)
			hostApi.createHosts("localhost", addHosts.values());
		if (updateHosts.size() > 0)
			hostApi.updateHosts("localhost", updateHosts.values());
		long lockEnd = new Date().getTime();

		long end = new Date().getTime();

		if (hosts.size() > 0)
			logger.trace(
					"kraken dom: added [{}] hosts, updated [{}] hosts, remained [{}] hosts, [{}]ms elapsed, [{}]ms lock, [{}] exist host",
					new Object[] { hosts.size() - existedHost.size(), existedHost.size(), queue.size(), end - begin,
							lockEnd - lockBegin, aleadyExist });
	}

	@Override
	public int getPendingCount() {
		return queue.size();
	}
}
