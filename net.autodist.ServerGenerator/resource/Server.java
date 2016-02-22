
import java.net.InetSocketAddress;

import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TNonblockingServer.Args;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

public class Server {
	
	public static void main(String[] args){		
		new Server();			
	}
	
	public Server()
	{
		try {
		    InetSocketAddress addr = new InetSocketAddress(HOST, PORT);
			TNonblockingServerSocket socket = new TNonblockingServerSocket(addr);
			Iface implementation=new ThriftServerImplementation();
			Processor<Iface> processor = new Processor<Iface>(implementation);
			Args args = new Args(socket).processor(processor);
			TServer server = new TNonblockingServer(args);
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
}
