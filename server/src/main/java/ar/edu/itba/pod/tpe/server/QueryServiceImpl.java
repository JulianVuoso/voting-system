package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.QueryService;

import java.rmi.RemoteException;

public class QueryServiceImpl implements QueryService {

    @Override
    public void ask_national() throws RemoteException, QueryException {

    }

    @Override
    public void ask_provincial(String state) throws RemoteException, QueryException {

    }

    @Override
    public void ask_table(Integer table) throws RemoteException, QueryException {

    }
}
