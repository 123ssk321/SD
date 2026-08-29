rm -f *.jks

keytool -genkey -alias users -keyalg RSA -validity 365 -keystore ./usersserver.jks -storetype pkcs12 << EOF
777users
777users
Users.Users
TP2
SD
LX
LX
PT
yes
777users
777users
EOF

echo
echo
echo "Exporting UsersServer Certificates"
echo
echo

keytool -exportcert -alias users -keystore usersserver.jks -file usersserver.cert << EOF
777users
EOF

echo "Adding UsersServer Certificates to Client Truststore"
cp cacerts client-ts.jks
keytool -importcert -file usersserver.cert -alias users -keystore client-ts.jks << EOF
changeit
yes
EOF

keytool -genkey -alias directory -keyalg RSA -validity 365 -keystore ./dirserver.jks -storetype pkcs12 << EOF
777directory
777directory
Directory.Directory
TP2
SD
LX
LX
PT
yes
777directory
777directory
EOF

echo
echo
echo "Exporting DirectoryServer Certificates"
echo
echo

keytool -exportcert -alias directory -keystore dirserver.jks -file dirserver.cert << EOF
777directory
EOF

echo "Adding DirectoryServer Certificates to Client Truststore"
#cp cacerts client-ts.jks
keytool -importcert -file dirserver.cert -alias directory -keystore client-ts.jks << EOF
changeit
yes
EOF

keytool -genkey -alias files -keyalg RSA -validity 365 -keystore ./filesserver.jks -storetype pkcs12 << EOF
777files
777files
Files.Files
TP2
SD
LX
LX
PT
yes
777files
777files
EOF

echo
echo
echo "Exporting FilesServer Certificates"
echo
echo

keytool -exportcert -alias files -keystore filesserver.jks -file filesserver.cert << EOF
777files
EOF

echo "Adding FilesServer Certificates to Client Truststore"
#cp cacerts client-ts.jks
keytool -importcert -file filesserver.cert -alias files -keystore client-ts.jks << EOF
changeit
yes
EOF