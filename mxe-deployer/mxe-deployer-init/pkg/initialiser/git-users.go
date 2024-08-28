package initialiser

import (
	"fmt"
	"os"
	"strings"

	"code.gitea.io/sdk/gitea"
	log "github.com/sirupsen/logrus"

	"mxe.ericsson/mxe-deploy-init/pkg/errors"
)

func CreateGitUsers() {
	var giteaRootUrl, adminUserName, adminPassword string
	var mxeUserName, mxePassword, mxeEmail string
	var authSourceId int64 = 1

	if giteaRootUrl = os.Getenv("GITEA_ROOT_URL"); len(giteaRootUrl) == 0 {
		errors.CheckError(fmt.Errorf("GITEA_ROOT_URL is empty"))
	}
	if adminUserName = os.Getenv("GITEA_ADMIN_USERNAME"); len(adminUserName) == 0 {
		errors.CheckError(fmt.Errorf("GITEA_ADMIN_USERNAME is empty"))
	}
	if adminPassword = os.Getenv("GITEA_ADMIN_PASSWORD"); len(adminPassword) == 0 {
		errors.CheckError(fmt.Errorf("GITEA_ADMIN_PASSWORD is empty"))
	}
	if mxeUserName = os.Getenv("MXE_USERNAME"); len(mxeUserName) == 0 {
		errors.CheckError(fmt.Errorf("MXE_USERNAME is empty"))
	}
	if mxePassword = os.Getenv("MXE_PASSWORD"); len(mxePassword) == 0 {
		errors.CheckError(fmt.Errorf("MXE_PASSWORD is empty"))
	}
	if mxeEmail = os.Getenv("MXE_EMAIL"); len(mxeEmail) == 0 {
		errors.CheckError(fmt.Errorf("MXE_EMAIL is empty"))
	}

	giteaClient, err := gitea.NewClient(giteaRootUrl)
	errors.CheckError(err)

	giteaClient.SetBasicAuth(adminUserName, adminPassword)

	mustChangePassword := false
	userOption := gitea.CreateUserOption{
		SourceID:           authSourceId,
		Username:           mxeUserName,
		LoginName:          mxeUserName,
		Password:           mxePassword,
		Email:              mxeEmail,
		MustChangePassword: &mustChangePassword,
	}

	_, userCreateResponse, err := giteaClient.AdminCreateUser(userOption)
	if err != nil {
		if strings.Contains(err.Error(), "user already exists") {
			log.Info("mxe user already exists")
			return
		}
		errors.CheckError(err)
	}
	log.Info("mxe user created successfully:", userCreateResponse.StatusCode)

	adminUser := true
	editUserOption := gitea.EditUserOption{
		SourceID:  userOption.SourceID,
		LoginName: userOption.LoginName,
		Password:  userOption.Password,
		Email:     &userOption.Email,
		Admin:     &adminUser,
	}
	userEditResponse, err := giteaClient.AdminEditUser(userOption.Username, editUserOption)
	if err != nil {
		log.Info("mxe user modified into admin:", userEditResponse.StatusCode)
		return
	}
	log.Info("mxe user modification to admin:", userEditResponse.StatusCode)
	errors.CheckError(err)
}
