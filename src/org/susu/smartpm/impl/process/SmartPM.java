package org.susu.smartpm.impl.process;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.susu.smartpm.api.process.Manageable;
import org.susu.smartpm.api.process.Manager;
import org.susu.smartpm.impl.utils.BlockedWorkerPool;

public class SmartPM implements Manager {
	Queue<Manageable> pluginManagers;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.susu.smartpm.api.process.Manageable#start()
	 */
	public boolean start() {
		pluginManagers = new ConcurrentLinkedQueue<>();

		// known manager can add at here
		Manageable tcpPm = new TcpHandlerManager();
		if (tcpPm.start()) {
			regist(tcpPm);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.susu.smartpm.api.process.Manageable#stop()
	 */
	public void stop() {
		Manageable pm;
		while ((pm = pluginManagers.poll()) != null) {
			pm.stop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Iterator<Manageable> it = pluginManagers.iterator();
		while (it.hasNext()) {
			BlockedWorkerPool.getInstance().schedule(it.next());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.susu.smartpm.api.process.Manager#regist(org.susu.smartpm.api.process
	 * .Manageable)
	 */
	@Override
	public boolean regist(Manageable member) {
		return pluginManagers.offer(member);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.susu.smartpm.api.process.Manageable#attachCtx(java.lang.Object)
	 */
	@Override
	public void attachCtx(Object ctx) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		Manager pm = new SmartPM();

		pm.start();

		BlockedWorkerPool.getInstance().schedule(pm);

		pm.stop();
	}

}
