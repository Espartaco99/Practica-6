package es.ucm.fdi.tp.practica6.test;

import es.ucm.fdi.tp.practica6.Main;

public class TestServer {
	public static void main(String[] args) {
		String[] as = { "-am", "server", "-g", "cn", "-p","X,O" };
		//-g at -p X,O --app-mode server --server-port 4000
		Main.main(as);
	}
}
