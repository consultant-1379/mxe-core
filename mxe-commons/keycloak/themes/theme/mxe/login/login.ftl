<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
    <#if section = "header">
        ${msg("doLogIn")}
    <#elseif section = "form">
    <div id="kc-form" <#if realm.password && social.providers??>class="${properties.kcContentWrapperClass!}"</#if>>
      <div id="kc-form-wrapper" <#if realm.password && social.providers??>class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}"</#if>>
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="${properties.kcFormGroupClass!}">
                    <#if usernameEditDisabled??>
                        <label id="form_login_username_label" for="username">Username</label>
                        <input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" type="text" disabled onclick="swapLabelAnimate()" placeholder="Email"/>
                    <#else>
                        <label id="form_login_username_label" for="username">Username</label>
                        <input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username" type="text" autofocus autocomplete="off" placeholder="Email" onclick="swapLabelAnimateUsername()" onfocusout="changeFocusOutLabelUsername()"/>
                    </#if>
                </div>

                <div class="${properties.kcFormGroupClass!}">
                      <label id="form_login_password_label" for="password" class="normal_q">Password</label>
                      <input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password" type="password" autocomplete="off" onfocus="swapLabelAnimatePassword()" onfocusout="changeFocusOutLabelPassword()"/>
                </div>

                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                    <div id="kc-form-options">
                        <#if realm.rememberMe && !usernameEditDisabled??>
                            <div class="checkbox">
                                <label>
                                    <#if login.rememberMe??>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                                    <#else>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
                                    </#if>
                                </label>
                            </div>
                        </#if>
                        </div>
                        <div class="${properties.kcFormOptionsWrapperClass!}">
                            <#if realm.resetPasswordAllowed>
                                <span><a id="forgot-password-link" tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                            </#if>
                        </div>

                  </div>

                  <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login-input" type="submit" value="${msg("doLogIn")}" />
                  </div>
            </form>
                <script>
                	let realDomain = window.location.protocol + "//" + window.location.host;
                	let div = document.createElement('textarea');
                	div.innerHTML = "${url.loginAction}";
                	let decodedUrl = div.firstChild.nodeValue;
                	let url = decodedUrl.replace(/http:\/\/eric-sec-access-mgmt-http:8080/gi, realDomain);
                	document.querySelector("#kc-form-login").action = url;
            	</script>
        </#if>
        </div>
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}">
                <hr/>
                <h4>${msg("identity-provider-login-label")}</h4>
                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 4>${properties.kcFormSocialAccountDoubleListClass!}</#if>">
                    <#list social.providers as p>
                        <li class="${properties.kcFormSocialAccountListLinkClass!}"><a href="${p.loginUrl}" id="zocial-${p.alias}" class="zocial ${p.providerId}"> <span>${p.displayName}</span></a></li>
                    </#list>
                </ul>
            </div>
        </#if>
      </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !usernameEditDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    </#if>
       

</@layout.registrationLayout>
