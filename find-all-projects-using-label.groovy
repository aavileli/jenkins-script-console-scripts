/*
    Copyright (c) 2015-2017 Sam Gleske - https://github.com/samrocketman/jenkins-script-console-scripts

    Permission is hereby granted, free of charge, to any person obtaining a copy of
    this software and associated documentation files (the "Software"), to deal in
    the Software without restriction, including without limitation the rights to
    use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
    the Software, and to permit persons to whom the Software is furnished to do so,
    subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
    COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
    IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
/*
   This reveals in Jenkins which projects are using a Jenkins agent with a
   particular label.  Useful for finding all projects using a particular set of
   agents.
*/
import hudson.model.Job

if(!binding.hasVariable('labels')) {
    labels = ['language:shell', 'language:ruby']
}
if(!binding.hasVariable('evaluateOr')) {
    //by default it searches for any of the labels from the list
    //set evaluateAnd to true to require all labels much exist in the job
    evaluateOr = true
}
if(!binding.hasVariable('formatJervis')) {
    //Are Jenkins jobs organized when generated by Jervis?
    //https://github.com/samrocketman/jervis
    formatJervis = false
}

//type check user defined parameters/bindings
if(!(labels instanceof List) || (false in labels.collect { it instanceof String } )) {
    throw new Exception('PARAMETER ERROR: labels must be a list of strings.')
}
if(!(evaluateOr instanceof Boolean)) {
    throw new Exception('PARAMETER ERROR: evaluateOr must be a boolean.')
}
if(!(formatJervis instanceof Boolean)) {
    throw new Exception('PARAMETER ERROR: formatJervis must be a boolean.')
}

projects = [] as Set
//getAllItems searches a global lookup table of items regardless of folder structure
Jenkins.instance.getAllItems(Job.class).each { i ->
    Boolean labelFound = false
    String jobLabelString
    if(i.class.simpleName == 'FreeStyleProject') {
        jobLabelString = i.getAssignedLabelString()
    } else if(i.class.simpleName == 'WorkflowJob') {
        jobLabelString = i.getDefinition().getScript()
    }
    List results = labels.collect { label ->
        jobLabelString.contains(label)
    }

    if(evaluateOr) {
        //evaluate if any of the labels exist in job
        labelFound = true in results
    } else {
        //evaluate requiring all labels to exist in job
        labelFound = !(false in results)
    }

    if(labelFound) {
        if(formatJervis) {
            projects << "${i.fullName.split('/')[0]}/${i.displayName.split(' ')[0]}"
        } else {
            projects << i.fullName
        }
    }
}
projects.each { println it }
//null so no result shows up
null
