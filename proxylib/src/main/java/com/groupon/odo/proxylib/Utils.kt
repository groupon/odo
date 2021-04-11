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

package com.groupon.odo.proxylib

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.ceil

object Utils {
    /**
     * Split string of comma-delimited ints into an a int array
     *
     * @param str
     * @return
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class)
    fun arrayFromStringOfIntegers(str: String?): IntArray {
        if (str == null) return IntArray(0)

        return str
                .split(",")
                .map { it.toInt() }
                .toIntArray()
    }

    fun getJQGridJSON(row: Any, rowName: String): HashMap<String, Any> {
        val returnVal = HashMap<String, Any>()
        returnVal["records"] = 1
        returnVal["total"] = 1
        returnVal["page"] = 1
        returnVal[rowName] = row
        return returnVal
    }

    fun getJQGridJSON(rows: Array<Any?>, rowName: String): HashMap<String, Any> {
        val returnVal = HashMap<String, Any>()
        returnVal["records"] = rows.size
        returnVal["total"] = 1
        returnVal["page"] = 1
        returnVal[rowName] = rows
        return returnVal
    }

    fun getJQGridJSON(rows: List<*>, rowName: String): HashMap<String, Any> {
        return getJQGridJSON(rows.toTypedArray(), rowName)
    }

    fun getJQGridJSON(rows: ArrayList<*>, rowName: String): HashMap<String, Any> {
        return getJQGridJSON(rows.toTypedArray(), rowName)
    }

    fun getJQGridJSON(rows: Array<Any?>, rowName: String, offset: Int, totalRows: Int, requestedRows: Int): HashMap<String, Any> {
        val returnVal = HashMap<String, Any>()
        val totalPages = ceil(totalRows.toDouble() / requestedRows.toDouble())
        returnVal["records"] = totalRows
        returnVal["total"] = totalPages
        returnVal["page"] = offset / requestedRows + 1
        returnVal[rowName] = rows
        return returnVal
    }

    /**
     * Copies file from a resource to a local temp file
     *
     * @param sourceResource
     * @return Absolute filename of the temp file
     * @throws Exception
     */
    @Throws(Exception::class)
    fun copyResourceToLocalFile(sourceResource: String?, destFileName: String?): File {
        return try {
            val keystoreFile: Resource = ClassPathResource(sourceResource)
            val inputStream = keystoreFile.inputStream

            val outKeyStoreFile = File(destFileName)
            val fop = FileOutputStream(outKeyStoreFile)
            val buf = ByteArray(512)
            var num: Int
            while (inputStream.read(buf).also { num = it } != -1) {
                fop.write(buf, 0, num)
            }
            fop.flush()
            fop.close()
            inputStream.close()
            outKeyStoreFile
        } catch (ioe: IOException) {
            throw Exception("Could not copy keystore file: " + ioe.message)
        }
    }

    /**
     * Returns the port as configured by the system variables, fallback is the default port value
     *
     * @param portIdentifier - SYS_*_PORT defined in Constants
     * @return
     */
    fun getSystemPort(portIdentifier: String): Int {
        var defaultPort = 0

        defaultPort = when(portIdentifier) {
            Constants.SYS_API_PORT -> Constants.DEFAULT_API_PORT
            Constants.SYS_DB_PORT -> Constants.DEFAULT_DB_PORT
            Constants.SYS_FWD_PORT -> Constants.DEFAULT_FWD_PORT
            Constants.SYS_HTTP_PORT -> Constants.DEFAULT_HTTP_PORT
            Constants.SYS_HTTPS_PORT -> Constants.DEFAULT_HTTPS_PORT
            else -> return defaultPort
        }

        val portStr = System.getenv(portIdentifier)
        return if (portStr == null || portStr.isEmpty()) defaultPort else Integer.valueOf(portStr)
    }

    /**
     * Returns the port as configured by teh system variables, fallback is the default port value
     *
     * @return
     */
    fun getEnvironmentOptionValue(option: String?): String? =
            System.getenv(option) // Pick the first non loop back address

    /**
     * This function returns the first external IP address encountered
     *
     * @return IP address or null
     * @throws Exception
     */
    fun getPublicIPAddress(): String? {
        val IPV4_REGEX = Regex("\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z")

        var ipAddr: String? = null
        val e: Enumeration<*> = NetworkInterface.getNetworkInterfaces()
        while (e.hasMoreElements()) {
            val n = e.nextElement() as NetworkInterface
            val ee: Enumeration<*> = n.inetAddresses
            while (ee.hasMoreElements()) {
                val i = ee.nextElement() as InetAddress

                // Pick the first non loop back address
                if (!i.isLoopbackAddress && i.isSiteLocalAddress ||
                        i.hostAddress.matches(IPV4_REGEX)) {
                    ipAddr = i.hostAddress
                    break
                }
            }
            if (ipAddr != null) {
                break
            }
        }
        return ipAddr
    }
}
