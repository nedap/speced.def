## Brief

## QA plan

## Author checklist

* [ ] I have QAed the functionality
* [ ] The PR has a reasonably reviewable size and a meaningful commit history
* [ ] I have run the [branch formatter](https://github.com/nedap/formatting-stack/blob/332a419034ab46fad526a5592f4257353bd695b6/src/formatting_stack/branch_formatter.clj) and observed no new/significative warnings
* [ ] I have self-reviewed the PR prior to assignment, and its build passes
* Specifically, I have code-reviewed iteratively the PR considering the following aspects in isolation:
  * [ ] Correctness
  * [ ] Robustness (red paths, failure handling etc)
  * [ ] Modular design
  * [ ] Test coverage
  * [ ] Spec coverage
  * [ ] Documentation
  * [ ] Security
  * [ ] Performance
  * [ ] Cross-compatibility (Clojure/ClojureScript)

## Reviewer checklist

* [ ] I have checked out this branch and reviewed it locally, running it
* [ ] I have QAed the functionality
* I have code-reviewed iteratively the PR considering the following aspects in isolation:
  * [ ] Business-facing correctness
  * [ ] Robustness (red paths, failure handling etc)
  * [ ] Modular design
  * [ ] Test coverage
  * [ ] Spec coverage
  * [ ] Documentation
  * [ ] Security
  * [ ] Performance
  * [ ] Cross-compatibility (Clojure/ClojureScript)
