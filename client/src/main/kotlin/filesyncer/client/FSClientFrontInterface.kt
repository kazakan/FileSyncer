package filesyncer.client

import filesyncer.common.file.FSFileMetaData

/** Interface that client should provide to ui class. */
interface FSClientFrontInterface {
    /**
     * Check connection with given server
     *
     * @param address address of server
     * @param port port number of server
     * @return whether succeed
     */
    fun checkConnection(address: String, port: Int): Boolean
    /**
     * Make User Login Request
     *
     * @param id userid
     * @param password
     * @param address address of server
     * @param port port number of server
     * @return whether succeed
     */
    fun requestLogin(id: String, password: String, address: String, port: Int): Boolean

    /**
     * Make User Register Request
     *
     * @param id userid
     * @param password
     * @param address address of server
     * @param port port number of server
     * @return whether succeed
     */
    fun requestRegister(id: String, password: String, address: String, port: Int): Boolean

    /**
     * Return file lists to show.
     *
     * List should be consisted of Map<String,String> And, default client will give following data
     * - name : file name
     * - status : status of file whether it is in local or cloud
     *     - "Only Cloud" : Only in cloud
     *     - "Only Local" : Only in local
     *     - "Both" : File is in bot cloud and local
     *     - "Err" : when something went wrong
     * - type : currently, only returns "file"
     *
     * @param dir folder to show
     * @return file lists type of `List<Map<String,String>>`
     */
    fun showFolder(dir: String): List<Map<String, String>>

    /**
     * Handle FileUpload request.
     *
     * @param path path of file to upload
     * @return whether succeed
     */
    fun uploadFile(path: String): Boolean

    /** Handle disconnect request. */
    fun disconnect()

    /**
     * Take message from client and pass it to UI. Return null if there's no message.
     *
     * @return message to show
     */
    fun takeReportMessage(): String?

    /** Get User registered in server */
    fun listUsers(): List<String>

    /** Request file share */
    fun shareFile(metadata: FSFileMetaData)
}
