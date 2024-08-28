package git

import "fmt"

func CommitMessageForPackage(resourceName string, resourceType string, action string, revision string, packageName string) string {
	return fmt.Sprintf("MXE Deployer %s %s in folder %s \n Checked in gitops manifests for %s", action, resourceType, resourceName, packageName)
}

func CommitMessageForPatchPackage(resourceName string, resourceType string, action string, revision string) string {
	return fmt.Sprintf("MXE Deployer %s %s in folder %s \n Patched gitops manifests", action, resourceType, resourceName)
}

func CommitMessageForArchive(folder string, revision string) string {
	return fmt.Sprintf("MXE Deployer added manifests to %s dir \n Checked in gitops manifests", folder)
}

func CommitMessageForDelete(app string, folder string, revision string) string {
	return fmt.Sprintf("MXE Deployer removed app %s \n Dir %s is deleted", app, folder)
}
