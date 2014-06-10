/*
 Copyright 2014 Groupon, Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package com.groupon.odo.proxylib.hostsedit.rmi;

import com.groupon.odo.proxylib.hostsedit.HostsFileUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MessageImpl extends UnicastRemoteObject implements Message {

    private static final long serialVersionUID = 1L;
    public MessageImpl() throws RemoteException {
    }

    @Override
    public void enableHost(String hostName) throws RemoteException {
        try {
            HostsFileUtils.enableHost(hostName);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void disableHost(String hostName) throws RemoteException {
        try {
            HostsFileUtils.disableHost(hostName);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public boolean exists(String hostName) throws RemoteException {
        try {
            return HostsFileUtils.exists(hostName);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(String hostName) throws RemoteException {
        try {
            return HostsFileUtils.isEnabled(hostName);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void removeHost(String hostName) throws RemoteException {
        try {
            HostsFileUtils.removeHost(hostName);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() throws RemoteException {
        return true;
    }
}