package es.ucm.fdi.tp.practica6.test;

import es.ucm.fdi.tp.practica6.Main;

public class TestClient {
	public static void main(String[] args) {
		String[] as = { "-am", "client" };
		//--app-mode client --server-host localhost --server-port 4000
		Main.main(as);
	}
}
