load.fun <- function(x) { 
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
  new.repos <- old.repos 
  new.repos["CRAN"] <- "http://cran.stat.ucla.edu" #set your favorite  CRAN Mirror here 
  options(repos = new.repos) 
  x <- as.character(substitute(x))
  dir.create(Sys.getenv("R_LIBS_USER"), recursive = TRUE) 
  eval(parse(text=paste("install.packages('", x, "', dep=TRUE)", sep=""))) 
  eval(parse(text=paste("require(", x, ")", sep=""))) 
}

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

if(!is.installed("ggplot2")){
	load.fun("ggplot2")
}

require(ggplot2)