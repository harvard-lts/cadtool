# CADTool #

This is a tool to perform identification, validation and metadata extraction on a limited set of CAD file formats.
It is designed to work with [FITS](http://projects.iq.harvard.edu/fits).

## Developer ##

### Building ###

CADTool uses [Maven](https://maven.apache.org/) to for compilation.
JDK (1.7 or later) and Maven will need to be installed and configured before compiling CAD Tool.
In order to build the necessary artifacts, run the following:

    mvn clean compile dependency:copy-dependencies package

That will put the CAD Tool jarfile (`cadtool-$VERSION.jar`), along with all of its dependencies (except for those already provided by FITS) in the `target` directory.

### Installation ###

Installation of CADTool into FITS requires a few steps:

* Create a `cadtool` directory inside of `${FITS_HOME}/lib`
* Copy the build artifacts into that directory (`cp target/*.jar ${FITS_HOME}/lib/cadtool`)
* Register the main tool class in `${FITS_HOME}/xml/fits.xml` under `<tools>` (`<tool class="edu.harvard.hul.ois.fits.tools.cad.CadTool" include="dxf,dwg,x3d,pdf"/>`)
* Add the CADTool lib directory to `compile.classpath` in `build.xml` (`<include name="lib/cadtool/*.jar"/>`)
* Add the CADTool lib directory to the generated classpath in `fits.bat` (`for %%i in (lib\cadtool\*.jar) do call "%FITS_HOME%\cappend.bat" %%i`)
* Add the CADTool lib directory to the generated classpath in `fits-env.sh`

      CTPATH=${FITS_HOME}/lib/cadtool
      for i in "$CTPATH"/*.jar; do
        APPCLASSPATH="$APPCLASSPATH":"$i"
      done

CADTool should now be integrated with the FITS toolchain and be invoked on any files with `dwg`, `dxf`, `x3d`, or `pdf` extensions.
