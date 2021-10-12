
```
oc new-project quarkus-play
oc new-project quarkus-play-uat
```

# Configure Jenkins
```
oc create sa jenkins -n quarkus-play
#oc policy add-role-to-user edit system:serviceaccount:quarkus-play:jenkins -n quarkus-play
oc policy add-role-to-user edit system:serviceaccount:quarkus-play:jenkins -n quarkus-play-uat
oc serviceaccounts get-token jenkins -n quarkus-play
```

