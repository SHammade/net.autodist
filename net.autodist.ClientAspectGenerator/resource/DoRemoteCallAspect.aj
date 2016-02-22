import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public aspect DoRemoteCallAspect {
	
	public static  int lb = -1;
	
	pointcut remoteCallMethod(net.autodist.annotation.RemoteCall anno) : call(* *(..)) && @annotation(anno);
	
	pointcut remoteCallMethodSum(net.autodist.annotation.RemoteCall anno, Double d1, Double d2) : 
		call(Double sum(..)) && @annotation(anno) && args(d1, d2) ;
	
	pointcut remoteCallMethodSumLB(net.autodist.annotation.RemoteCalls anno, Double d1, Double d2) : 
		call(Double sum(..)) && @annotation(anno) && args(d1, d2) ;
	
	Double around(net.autodist.annotation.RemoteCall anno, Double d1, Double d2) : remoteCallMethodSum(anno,d1, d2){
		TTransport transport = new TFramedTransport(new TSocket(anno.servername(), anno.serverport()));
	    TProtocol protocol = new TBinaryProtocol(transport);
	    Client client = new Client(protocol);
	    double sum = -1;
		try {
			transport.open();
			sum = client.sum(d1, d2);
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
		transport.close();		
		return sum;
	}

	Double around(net.autodist.annotation.RemoteCalls anno, Double d1, Double d2) : remoteCallMethodSumLB(anno,d1, d2){
		RemoteCall[] rc = anno.value();
		
		int val = getLBval(rc.length);//(int)(Math.random() * rc.length);
		
		TTransport transport = new TFramedTransport(new TSocket(rc[val].servername(), rc[val].serverport()));
	    TProtocol protocol = new TBinaryProtocol(transport);
	    Client client = new Client(protocol);
	    double sum = -1;
		try {
			transport.open();
			sum = client.sum(d1, d2);
		} catch (TException | TTransportException e) {
			e.printStackTrace();
		}
	    
	    Logger.log("Remote executed: "+d1+"+"+d2+"="+sum+" on "+rc[val].servername()+":"+rc[val].serverport());
	    transport.close();
		
		return sum;
	}
	
	public synchronized int getLBval(int i){
		if (lb == -1){
			lb = 0;
		} else if(lb == i-1) {
			lb = 0;
		} else {
			lb++;
		}
		return lb;
	}
}
