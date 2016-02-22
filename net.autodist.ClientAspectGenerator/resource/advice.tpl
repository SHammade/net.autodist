	METHODRETURNTYPE around(net.autodist.annotation.RemoteCall anno METHODARGSDEF) : remoteCallMethodMETHODNAME(anno METHODDEFARGSLIST){
		TTransport transport = new TFramedTransport(new TSocket(anno.servername(), anno.serverport()));
	    TProtocol protocol = new TBinaryProtocol(transport);
	    Client client = new Client(protocol);
	    
	    METHODRETURNDECLARATION
		try {
			transport.open();
			METHODTHRIFTCALL client.METHODNAME(METHODARGSLIST);
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		} finally {
			transport.close();
		}
		METHODRETURNVALUE
	}
