package ar.edu.itba.pod.tpe;

import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.server.ElectionServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;

import static ar.edu.itba.pod.tpe.models.Status.*;
import static org.junit.Assert.assertEquals;

public class ManagementTest {

    private ElectionServiceImpl service;


    @Before
    public final void before(){
        service = new ElectionServiceImpl();
    }

    @Test
    public final void testOpenElection() throws ManagementException, RemoteException {

        service.open();

        assertEquals( OPEN , service.status());

    }

    @Test(expected = ManagementException.class)
    public final void testExceptionWhenClose() throws ManagementException, RemoteException {
        service.close();
    }

    @Test
    public final void testCloseElection() throws ManagementException, RemoteException {
        service.open();

        service.close();

        assertEquals(CLOSE,service.status());
    }


    @Test(expected = ManagementException.class)
    public final void testDoubleOpenElection() throws ManagementException, RemoteException {
        service.open();
        service.open();
    }

    @Test
    public final void testUndefinedStatus() throws RemoteException{
        assertEquals(UNDEFINED,service.status());
    }

    @Test(expected = ManagementException.class)
    public final void testDoubleCloseElection()throws ManagementException, RemoteException{

        service.open();

        service.close();
        service.close();
    }

    @Test
    public final void testOpenReturnValue() throws ManagementException, RemoteException{
        assertEquals(STARTED,service.open());
    }

    @Test
    public final void testCloseReturnValue() throws ManagementException, RemoteException{
        service.open();
        assertEquals(ENDED,service.close());
    }

}
