package com.fatihsevban.instakotlinapp.Utils

import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.util.*
import android.os.Environment.getExternalStorageDirectory
import java.io.FileOutputStream


class FileOperations {

    companion object {

        /**
         * gets the documents Url's in a specific folder.
         * @param folderName is the folder name.
         * @return the Url's of the items of the folder.
         */
        fun getDocsInFolder(folderName:String): ArrayList<String>{

            val allDocuments = ArrayList<String>()
            val file = File(folderName)

            // taking all documents from the specified folder.
            val allDocumentsInTheFolder = file.listFiles()

            // checking whether there is document in the folder.
            if(allDocumentsInTheFolder != null) {

                // galeriden getirilen resimlerin tarihe göre sondan basa listelenmesi
                if(allDocumentsInTheFolder.size > 1) {

                    Arrays.sort(allDocumentsInTheFolder) { file1, file2 ->

                        if(file1!!.lastModified() > file2!!.lastModified()){
                            -1
                        }else {
                            1
                        }
                    }
                }


                for (i in 0 until allDocumentsInTheFolder.size) {

                    // looking only to the files
                    if(allDocumentsInTheFolder[i].isFile){

                        // contains the readed documents name and the place in the phone.
                        // files://root/logo.png

                        val documentPath = allDocumentsInTheFolder[i].absolutePath
                        val lastPointIndex = documentPath.lastIndexOf(".")

                        if(lastPointIndex > 0){

                            val documentType = documentPath.substring(lastPointIndex)

                            // getting the folders with the requested extension.
                            if( documentType.equals(".jpg") || documentType.equals(".jpeg")
                                    || documentType.equals(".png") || documentType.equals(".mp4")) {

                                allDocuments.add(documentPath)
                            }
                        }
                    }
                }
            }

            return allDocuments

        }

        /**
         * checks whether a directory exists in a phone or not.
         * @param folderName is the directory that will be checked.
         */
        fun doesFolderExist(folderName:String): Boolean {

            val file = File(folderName)

            // taking all documents from the specified folder.
            val allDocumentsInTheFolder = file.listFiles()

            return (allDocumentsInTheFolder != null)
        }

        /**
         * saves cropped bitmap to external storage.
         * @param bitmap is the cropped bitmap.
         * @return the cropped images uri in the phone.
         */
        fun saveCroppedBitmapToExternalStorage(bitmap: Bitmap): String? {

            // creating a destination folder for the compressed file.
            val root = Environment.getExternalStorageDirectory().absolutePath
            val myDir = File(root + "/Compressed/Cropped")
            myDir.mkdirs()

            if (myDir.isDirectory || myDir.mkdirs()) {

                val n = System.currentTimeMillis().toString().substring(8)
                val fname = "Cropped_$n.jpg"
                val file = File(myDir, fname)
                if (file.exists())
                    file.delete()
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return file.absolutePath

            }

            return null
        }

    }
}