1. In Debian OS:
```
sudo apt install git maven -y
git clone https://github.com/zhmichael007/sts_gcs.git
```
2. Create a Service Account, grant "Storage Object Admin" priviledge to it and dowanload a key file. Storage Object Admin ( roles/storage.objectAdmin ), Grants full control over objects, including listing, creating, viewing, and deleting objects.

3. Rename the key file to "gcs_sa_key.json" and put it to the same folder as pom.xml 

4. Compile and run the java code:
```
mvn compile
mvn exec:java -Dexec.mainClass=com.google.cloud.auth.samples.DownscopingExample
```

5. Get the access token from the output and try the curl command:

Upload a file via the access token and curl:
```
echo 'test' > demo.json
TOKEN="<access token>"
curl -X POST -T ./demo.json \
    -H "Authorization: Bearer $TOKEN" \
    "https://storage.googleapis.com/upload/storage/v1/b/<Your Bucket Name>/o?name=device1/1/2/3/test1/demo.json"
```

You will see the json output to show the upload is successful.

6. Test other folder priviledge:
try this command:
```
curl -X POST -T ./demo.json \
    -H "Authorization: Bearer $TOKEN" \
    "https://storage.googleapis.com/upload/storage/v1/b/<Your Bucket Name>/o?name=device1/1/2/4/test1/demo.json"
```

You will see the message:

"message": "xxx.iam.gserviceaccount.com does not have storage.objects.create access to the Google Cloud Storage object.



GCS upload and download via curl please refer to:

https://cloud.google.com/storage/docs/uploading-objects

https://cloud.google.com/storage/docs/downloading-objects


