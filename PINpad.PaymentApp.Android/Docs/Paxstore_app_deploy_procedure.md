## PAXStore App Deployment Procedure
---

The following document demonstrates how to deploy applications through Sektor PAXStore. 

In brief: Developers will create a sandbox release to QA team, QA team has to approve the release on PAXStore for it to be deployed to Customers/External QA. 

### Prerequisites

User must have:

1. Sektor [PAXStore](sektor.whatspos.com/) developer account
2. Access to [PAXStore Administrator Center](https://sektor.whatspos.com/admin) or prod side of Sektor
3. All test terminals should be in **debug mode**. Releases to prod terminals will involve signing applications at PPN. 

### Deploying to Internal QA Team

For the purposes of ensuring that Internal QA Team always approves a release before it is deployed to the customer, the following steps are advised:

1. Developer making the release should upload all APKs generated through pipeline onto [PAXStore Developer Center App Management](https://sektor.whatspos.com/developer#/applications)
2. **Do not** click on 'Submit for Approval' button. Click on 'Save & Sandbox Test' for each APK being uploaded. 
3. Follow the steps on the screen for creating a push onto a test terminal for Internal QA team. 
4. Select the terminal Serial ID for the QA team. This is the terminal for Sandbox testing
5. Configure the terminal with any relevant information. Activate the push when ready. 
6. This will create a push onto the QA team member's terminal. 

### Deploying to External QA Team

- This step can only be done if Internal QA Approves the app after sandbox testing. 
- This has to be done by the QA team to go to PAXStore -> Developer Center -> App Management -> App -> Submit for Approval. 
- Once the QA team does this, the app should be ready to be released to external QA team. 
- Navigate to Administrator Center on PAXStore. Click on the 'Terminal Management' from the sidebar
- Click on the Customer merchant name from under Linkly. Currently it is 'Woolworths POC'. 
- Browse the Terminals for testing under 'Terminal List'
- Select the terminal to be released to. 
- Under 'App & Firmware', there is a button for 'Push App'. Click on the button
- Select the apps to be released with the latest release versions. 
- This will start the app pushes, but won't activate it. User making the release will have to manually activate each of the app push 
- If an app requires parameters, they have to be entered here. 


### Deploying to Prod terminals

TBA

---