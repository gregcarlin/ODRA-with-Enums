package tests;

import odra.exceptions.rd.RDCompilationException;
import odra.network.transport.DBConnection;
import odra.network.transport.DBReply;
import odra.network.transport.DBRequest;
import odra.sbql.results.runtime.RawResultPrinter;

public class NetworkTest {
	private void bigRequest() throws Exception {
		// nawiazujemy polaczenie z baza
		DBConnection conn = new DBConnection("localhost", 1521);
		conn.connect();

		// logujemy sie
		DBRequest lreq = new DBRequest(DBRequest.LOGIN_RQST, new String[] { "admin", "admin"} );
		conn.sendRequest(lreq);

//		lreq = new DBRequest(DBRequest.LOAD_DATA_RQST, new String[] { new String(new byte[3000]) } );
//		conn.sendRequest(lreq);
	}

	private void begin() {
		try {
			// nawiazujemy polaczenie z baza
			DBConnection conn = new DBConnection("localhost", 1521);
			conn.connect();

			// logujemy sie
			DBRequest lreq = new DBRequest(DBRequest.LOGIN_RQST, new String[] { "admin", "admin"} );
			conn.sendRequest(lreq);

			// tworzymy dwa moduly
			DBRequest mreq1 = new DBRequest(DBRequest.ADD_MODULE_RQST, new String[] { "module dane { x : integer; }", "admin" } );
			conn.sendRequest(mreq1);

			DBRequest mreq2 = new DBRequest(DBRequest.ADD_MODULE_RQST, new String[] { "module procedury { import admin.dane; proc() : integer { return x; } }", "admin"} );
			conn.sendRequest(mreq2);

			// kompilujemy modul procedury (modul dane skompiluje sie sam)
			DBRequest creq = new DBRequest(DBRequest.COMPILE_RQST, new String[] { "admin.dane" } );
			conn.sendRequest(creq);

			// wywolujemy procedure proc z modulu procedury
			DBRequest rreq = new DBRequest(DBRequest.EXECUTE_SBQL_RQST, new String[] { "proc();", "admin.procedury", "default", "off" } );
			DBReply rply = conn.sendRequest(rreq);

			// wyswietlamy wynik
			RawResultPrinter resprt = new RawResultPrinter();
			System.out.println(resprt.print(rply.getResult()));

			// zamykamy polaczenie
			conn.close();
		}
		catch (RDCompilationException ex) {
			System.out.println("blad: " + ex.getMessage());
			System.out.println("linia: " + ex.getLine());
			System.out.println("kolumna: " + ex.getColumn());
			System.out.println("modul: " + ex.getModule());

			ex.printStackTrace();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		new NetworkTest().begin();

		System.out.println("Koniec testu");
	}
}
