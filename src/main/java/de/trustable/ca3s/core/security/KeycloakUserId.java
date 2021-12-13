package de.trustable.ca3s.core.security;

import java.io.Serializable;

/**
 *
 *     {"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJYX0xGVjg1MFVsNm01WGt5WF9jbzRaRVcyRnliWjN3VjB0cl9laVJwWW9FIn0.eyJleHAiOjE2MzgxMzM2NzUsImlhdCI6MTYzODEzMzM3NSwianRpIjoiYjRkNjc3MzItODE0ZS00NTIwLTljNTktYWRjNjY3NWZmMzQ5IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo1MDA4MC9hdXRoL3JlYWxtcy9jYTNzUmVhbG0iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOTVkM2Y3NTMtY2E4OC00ZWIxLTljNjctZDM3NjZiNGU3YmZmIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiY2EzcyIsInNlc3Npb25fc3RhdGUiOiIwMjA0NGIzNi1lMTIwLTRiZmUtODRlOC01MjMwNzAyMmJjZTUiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLWNhM3NyZWFsbSIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJjYTNzIjp7InJvbGVzIjpbInVzZXIiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjAyMDQ0YjM2LWUxMjAtNGJmZS04NGU4LTUyMzA3MDIyYmNlNSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IlVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJrY3VzZXIiLCJnaXZlbl9uYW1lIjoiVXNlciIsImZhbWlseV9uYW1lIjoiIiwiZW1haWwiOiJ1c2VyQHRydXN0YWJsZS5kZSJ9.QOYckEvFPr_6-DitnhzzcZG5ipp9TVTpz_cvCpFhKMQCHj5OI2sj1sd0lyvO7ODUMVsVd2FoGOIyIq94pKVquh-BawZHr-lLxZ0UG0Jv8e2wlTKTiG4mwLo3LYKtK6p-UsA-HpPfqIEw5Pbbil_cln4om_4K8KF5H7OkR2tVk2HnqIx8RJBX7hdYZETViCLuyUPpuEFOjfCAgnuGQotMKHCuUxGkAyLaSmMNPlnqNu2RPfawCFGcCeR8nXtEM2ToU0hNOFmayYhtwJS07tzKe11DfoS6To7aNWnWqFihmc9Q6SVFfHGuT4AxKp7P53-r_05f6bLlJfTqvw2XTeoNwQ",
 *         "expires_in":300,
 *         "refresh_expires_in":1800,
 *         "refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJiNmQ2ZGJlZi1jMTdiLTQxZjAtOWVkOC00NmY3Y2IwY2Y3MzIifQ.eyJleHAiOjE2MzgxMzUxNzUsImlhdCI6MTYzODEzMzM3NSwianRpIjoiZTMwNDRiOWMtMWUwYy00NGFmLThlM2YtNTk0Yzg5Njg2N2E3IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo1MDA4MC9hdXRoL3JlYWxtcy9jYTNzUmVhbG0iLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjUwMDgwL2F1dGgvcmVhbG1zL2NhM3NSZWFsbSIsInN1YiI6Ijk1ZDNmNzUzLWNhODgtNGViMS05YzY3LWQzNzY2YjRlN2JmZiIsInR5cCI6IlJlZnJlc2giLCJhenAiOiJjYTNzIiwic2Vzc2lvbl9zdGF0ZSI6IjAyMDQ0YjM2LWUxMjAtNGJmZS04NGU4LTUyMzA3MDIyYmNlNSIsInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsInNpZCI6IjAyMDQ0YjM2LWUxMjAtNGJmZS04NGU4LTUyMzA3MDIyYmNlNSJ9.ca8abv5ZPtvmDMUQKMwxksPsUJDOadpvWbXwTavf2tg",
 *         "token_type":"Bearer",
 *         "not-before-policy":0,
 *         "session_state":"02044b36-e120-4bfe-84e8-52307022bce5",
 *         "scope":"profile email"}
 *
 *
 *         {
 *    "access_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJYX0xGVjg1MFVsNm01WGt5WF9jbzRaRVcyRnliWjN3VjB0cl9laVJwWW9FIn0.eyJleHAiOjE2Mzg2Mzk2NDksImlhdCI6MTYzODYzOTM0OSwiYXV0aF90aW1lIjoxNjM4NjM5MDQ0LCJqdGkiOiI5Mjk3YTQzMi1lY2I2LTQ0ZjktYTE2Ni05YjgwM2NiMmQ4NDMiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjUwMDgwL2F1dGgvcmVhbG1zL2NhM3NSZWFsbSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI5NWQzZjc1My1jYTg4LTRlYjEtOWM2Ny1kMzc2NmI0ZTdiZmYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJjYTNzIiwic2Vzc2lvbl9zdGF0ZSI6IjgyNzYyYzc2LTA1ZDItNDY5Yi04ZjkxLTcyNjliODg2MGMwZSIsImFjciI6IjAiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiIsImRlZmF1bHQtcm9sZXMtY2Ezc3JlYWxtIiwidXNlciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImNhM3MiOnsicm9sZXMiOlsidXNlciJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsInNpZCI6IjgyNzYyYzc2LTA1ZDItNDY5Yi04ZjkxLTcyNjliODg2MGMwZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IlVzZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJrY3VzZXIiLCJnaXZlbl9uYW1lIjoiVXNlciIsImZhbWlseV9uYW1lIjoiIiwiZW1haWwiOiJ1c2VyQHRydXN0YWJsZS5kZSJ9.Y02dyBsGYN9xJ_NOqlCB7UGk9CPFpkcVxNmIDo6nuDjvd8gwNAz4Q16avfMpF4-2mYwitxLOAB6etcaUI1GaHVrI9NWs1voGj4gA652bD6Z9mN0Mq9MqMZ3GrmcSUdJCjLCdwP15r8CvV77FDs77xlDxmYr6pjZ7jqWxcVUfU0gDmyNCSYlTkWPdFR6guP2kghgjKypwa-NtE7tKt1BBc_BprFB5FX5kuLuzD3ZkT95NWwXOSlYNRM3yaHczFkzwJqzXRp2hx4Dnj1v7PVNx1X_-f3b3vtg6jZgjGRZpqZDZAHBGFcrTLQwOSg09Yr4wHJ8QfLk2x-42XRBUlyvtJQ",
 *    "expires_in":300,
 *    "refresh_expires_in":0,
 *    "token_type":"Bearer",
 *    "id_token":"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJYX0xGVjg1MFVsNm01WGt5WF9jbzRaRVcyRnliWjN3VjB0cl9laVJwWW9FIn0.eyJleHAiOjE2Mzg2Mzk2NDksImlhdCI6MTYzODYzOTM0OSwiYXV0aF90aW1lIjoxNjM4NjM5MDQ0LCJqdGkiOiJhMTRmMTcyZC0xM2Q1LTQ4MzktYTlmYS0yYjJkOGE1MmVmODUiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjUwMDgwL2F1dGgvcmVhbG1zL2NhM3NSZWFsbSIsImF1ZCI6ImNhM3MiLCJzdWIiOiI5NWQzZjc1My1jYTg4LTRlYjEtOWM2Ny1kMzc2NmI0ZTdiZmYiLCJ0eXAiOiJJRCIsImF6cCI6ImNhM3MiLCJzZXNzaW9uX3N0YXRlIjoiODI3NjJjNzYtMDVkMi00NjliLThmOTEtNzI2OWI4ODYwYzBlIiwiYXRfaGFzaCI6IlR2NjNMZ0FlSy0tY1JTbUdVVWZYa2ciLCJhY3IiOiIwIiwic2lkIjoiODI3NjJjNzYtMDVkMi00NjliLThmOTEtNzI2OWI4ODYwYzBlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6ImtjdXNlciIsImdpdmVuX25hbWUiOiJVc2VyIiwiZmFtaWx5X25hbWUiOiIiLCJlbWFpbCI6InVzZXJAdHJ1c3RhYmxlLmRlIn0.KYicit4MVWKPsw7mXVfQIqLlyavLK-U5pBZN1E7G4saI1ohtxnAmozBKYOExJRKyYJomE65vi7xl3Kido_gB29UhJ43LN0Nv6LnQAKHC4lPOoHv3AAa6DBbWpNU1SVsPWH9mFR4k_d-HWeuDxevZXlIXkJwhXcPLLa8nlVdWLPeugS2mnw24z4iUD1ILYQFn1p7f_H7KnoBEfL0XLQh5av9Mkm1K1teCOeLCLtQKDf-QDfbsX8IfGq8ry1Fq5O8rTfFvJKk_S2p1tJ4W14arC-UqNQvDmcDxaPeQIBJN_W6J8cQUUIEAVXF9GcPip_EWYxAgH5tVmvKE_SkjVMy8ww",
 *    "not-before-policy":0,
 *    "session_state":"82762c76-05d2-469b-8f91-7269b8860c0e",
 *    "scope":"openid profile email"
 * }
 */
public class KeycloakUserId implements Serializable {

    private String token_type;
    private String access_token;
    private String refresh_token;
    private String session_state;
    private String scope;
    private int expires_in;
    private int refresh_expires_in;
//    private int not-before-policy;

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getSession_state() {
        return session_state;
    }

    public void setSession_state(String session_state) {
        this.session_state = session_state;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public int getRefresh_expires_in() {
        return refresh_expires_in;
    }

    public void setRefresh_expires_in(int refresh_expires_in) {
        this.refresh_expires_in = refresh_expires_in;
    }

    public KeycloakUserId(){}


}
