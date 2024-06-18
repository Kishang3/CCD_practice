(config
(text-field
:name "clintid"
:label "clintid"
:placeholder "Please enter your clintid hear ")

(password-field
:name "clintsecret"
:label "clintsecret"
:placeholder "Please enter your clintsecret hear ")

(oauth2/authorization-code-flow-client-credentials
(token
(source
(http/post
:url "https://customer.my.onetrust.com/api/access/v1/oauth/token"
(body-params
"grant_type"  "client_credentials"
"client_id"  "{CLIENT-ID}"
"client_secret"  "${CLIENT-SECRET}"
)))

 (fields
  access_token :<= "access_token"
  refresh_token
  expires_in   :<= "expires_in"
  scope    :<= "scope"
  realm_id
  token_type  :<= "token_type"
  ))))



(default-source (http/get :base-url " https://customer.my.onetrust.com/api/document/v1"
(header-params "Content-Type" "application/json"))
(paging/no-pagination)
(auth/oauth2)
(error-handler
(when :status 404 :message "not found" :action fail)
(when :status 404 :action skip)
(when :status 429 :action rate-limit)
(when :status 401 :action refresh)
(when :status 503 :action retry))
)

 (entity DATA_SUBJECT
 (api-docs-url "https://developer.onetrust.com/onetrust/reference/getv3datasubjectprofilesusingpost")
 (source (http/get :url "rest/api/preferences/v3/datasubject-profiles")
   (extract-path "content")
  (paging-scheme page-number
        :Page-number-query-param-initial-value  0,
        :page-number-query-param-name "page" , 
  )
  (query-params  "collectionPointGuid" "{collection.id}"
                 "purposeGuid" "{purpose.id}")

  
 )
 
 ;;sync plan exicuted
 (sync-plan
          (change-capture-cursor
            (query-params "sort" "desc")
            (extract-path "content")
           (subset/by-time (query-params "updatedSince" "$FROM"
                                         "updatedUntil" "$TO")
                           (format "yyyy-MM-dd'T'HH:mm:ssZ")
                           (step-size "24 hr")
                           (initial  "2023-01-01T00:00:00Z")
                        )))
 )