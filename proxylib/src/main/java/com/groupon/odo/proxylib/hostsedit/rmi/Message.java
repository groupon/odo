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

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Message extends Remote {
    void removeHost(String hostName) throws RemoteException;

    void enableHost(String hostName) throws RemoteException;

    void disableHost(String hostName) throws RemoteException;

    boolean exists(String hostName) throws RemoteException;

    boolean isEnabled(String hostName) throws RemoteException;

    boolean isAvailable() throws RemoteException;
}