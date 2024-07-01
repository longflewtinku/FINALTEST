@.\XSDDocumenter.exe ../payment/src/main/assets/initialparams.xsd ../documentation
@.\XSDDocumenter.exe ../payment/src/main/assets/overrideparams.xsd ../documentation
@.\XSDDocumenter.exe ../payment/src/main/assets/hotloadparams.xsd ../documentation

@echo ==================================================
@if %errorlevel%==0 goto success
@echo XML Documentation Failed
@echo ==================================================
pause
@exit /b

:success
@echo XML Documentation Successful
@echo ==================================================