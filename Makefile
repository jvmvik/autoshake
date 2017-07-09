# JavaFx application packaging
#

app_name=autoshake
jar=out/artifacts/autoshake/autoshake.jar

all: dist

dist:
	javapackager -deploy -native -outdir out/dist -outfile $(app_name) \
		-srcdir src -srcfiles out/artifacts/autoshake/autoshake.jar -appclass vtool.run.Main \
		-name "Autoshake" -title "Autoshake"