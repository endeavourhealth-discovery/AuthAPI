delete from config.config where app_id='authAPI' and config_id='keycloak';
insert into config.config (app_id, config_id, config_data)
select 'authAPI', 'keycloak', 
'{
   "realm": "endeavour2",
   "auth-server-url": "https://devauth.endeavourhealth.net/",
    "path_prefix":"auth/realms",
	"path_suffix":"protocol/openid-connect/token"
 }';