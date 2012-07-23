package org.susu.smartpm.impl.process;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.susu.smartpm.api.process.Manageable;
import org.susu.smartpm.api.process.Manager;
import org.susu.smartpm.impl.utils.BlockedWorkerPool;

public class TcpHandlerManager implements Manager {
	Queue<Integer> newPorts;
	Map<Integer, Manageable> portManagers;
	Selector selector;

	void registSvrChannel(int port) throws Throwable {
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(port));
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	void activeKeyHandle(SelectionKey key) throws Throwable {
		if (key.isValid()) {
			if (key.isAcceptable()) {
				ServerSocketChannel channel = (ServerSocketChannel) key
						.channel();
				SocketChannel socketChannel = channel.accept();
				socketChannel.configureBlocking(false);
				socketChannel.register(selector, SelectionKey.OP_READ);
			}

			if (key.isReadable()) {
				SocketChannel socketChannel = (SocketChannel) key.channel();
				int port = new Integer(
						((InetSocketAddress) socketChannel.getLocalAddress())
								.getPort());
				Manageable handler = portManagers.get(port);
				if (handler != null) {
					handler.attachCtx(socketChannel);
					BlockedWorkerPool.getInstance().schedule(handler);
				}
			}
		}
	}

	@Override
	public void run() {
		Integer port;

		try {
			while (selector.isOpen()) {
				while ((port = newPorts.poll()) != null) {
					registSvrChannel(port.intValue());
				}

				selector.select();

				Iterator<SelectionKey> keyIterator = selector.selectedKeys()
						.iterator();
				while (keyIterator.hasNext()) {
					activeKeyHandle(keyIterator.next());
					keyIterator.remove();
				}
			}

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean start() {
		newPorts = new ConcurrentLinkedQueue<Integer>();
		portManagers = new ConcurrentHashMap<Integer, Manageable>();

		try {
			selector = Selector.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		EchoHandler handler = new EchoHandler();
		handler.setServerPort(1234);
		this.regist(handler);
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean regist(Manageable member) {
		Integer serverPort = new Integer(
				((EchoHandler) member).getServerPort());
		newPorts.offer(serverPort);
		portManagers.put(serverPort, member);
		return false;
	}

	@Override
	public void attachCtx(Object ctx) {
		// TODO Auto-generated method stub

	}

}
