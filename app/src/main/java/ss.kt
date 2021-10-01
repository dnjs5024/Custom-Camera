class ss {

    val inputStream = resources.openRawResource(R.raw.my_image)
    val filePath = "$filesDir/my_image.jpg"
    val outputStream = FileOutputStream(filePath)
    while (true) {
        val data = inputStream.read()
        if (data == -1) {
            break
        }
        outputStream.write(data)
    }
    inputStream.close()
    outputStream.close()


}