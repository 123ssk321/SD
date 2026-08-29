rm -f *.jks

keytool -genkey -alias users -keyalg RSA -validity 365 -keystore ./users.jks -storetype pkcs12 << EOF
123users
123users
Users.Users
TP2
SD2021
LX
LX
PT
yes
123users
123users
EOF

echo
echo
echo "Exporting Certificates"
echo
echo

keytool -exportcert -alias users -keystore users.jks -file users.cert << EOF
123users
EOF

echo "Creating Client Truststore"
cp cacerts client-ts.jks
keytool -importcert -file users.cert -alias users -keystore client-ts.jks << EOF
changeit
yes
EOF

keytool -genkey -alias directory -keyalg RSA -validity 365 -keystore ./directory.jks -storetype pkcs12 << EOF
123directory
123directory
Users.Users
TP2
SD2021
LX
LX
PT
yes
123users
123users
EOF

echo
echo
echo "Exporting Certificates"
echo
echo

keytool -exportcert -alias directory -keystore directory.jks -file directory.cert << EOF
123users
EOF

echo "Creating Client Truststore"
cp cacerts client-ts.jks
keytool -importcert -file directory.cert -alias directory -keystore client-ts.jks << EOF
changeit
yes
EOF

keytool -genkey -alias files -keyalg RSA -validity 365 -keystore ./files.jks -storetype pkcs12 << EOF
123users
123users
Users.Users
TP2
SD2021
LX
LX
PT
yes
123users
123users
EOF

echo
echo
echo "Exporting Certificates"
echo
echo

keytool -exportcert -alias files -keystore files.jks -file files.cert << EOF
123users
EOF

echo "Creating Client Truststore"
cp cacerts client-ts.jks
keytool -importcert -file files.cert -alias files -keystore client-ts.jks << EOF
changeit
yes
EOF