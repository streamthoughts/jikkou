---
title: Jikkou
linkTitle: Jikkou
---

<script>
$(function () {
  count = 0;
  wordsArray = ["Apache Kafka", "Everything", "________?"];
  setInterval(function () {
    count++;
    $("#hero-title-end").fadeOut(400, function () {
      $(this).text(wordsArray[count % wordsArray.length]).fadeIn(400);
    });
  }, 2000);
});
</script>

{{% blocks/cover title="Jikkou (jikkō / 実行)!" image_anchor="top" height="max" color="white" %}}
<div class="mx-auto">
	<h2 class="hero-title mb-5">The <span class="text-light">Open source</span> Resource<br /> as Code framework for <br /><span id="hero-title-end" class="hero-end">Apache Kafka</span></h2>
	<a class="btn btn-lg btn-secondary mx-4" href="">
        Learn More <i class="fas fa-arrow-alt-circle-right ml-2"></i>
    </a>
	<a class="btn btn-lg btn-github mx-4" href="https://github.com/streamthoughts/jikkou">
		Download <i class="fab fa-github ml-2 "></i>
	</a>
</div>
{{% /blocks/cover %}}

{{% blocks/showcase color="white" %}}
{{% asciinema key="demo" autoPlay="true" loop="true" fit="none" rows="45" cols="200" terminalFontSize="15px" terminalLineHeight="1.1" theme="solarized-dark" %}}
{{% /blocks/showcase %}}

{{% blocks/section type="row features"%}}
{{% blocks/feature icon="fas fa-globe-asia" title="Declarative & Automated" %}}
Describe the entire desired state of any resource you need to manage using [YAML](https://yaml.org/) descriptor files.
{{% /blocks/feature %}}

{{% blocks/feature icon="fas fa-medal" title="Designed for Apache Kafka&reg;" %}}
Jikkou was initially developed to manage Apache Kafka resources. You can use it with most of Apache Kafka
vendors: [Apache Kafka](https://kafka.apache.org/), [Aiven](https://aiven.io/kafka), [Amazon MSK](https://aws.amazon.com/fr/msk/), [Confluent Cloud](https://www.confluent.io/confluent-cloud/), [Redpanda](https://redpanda.com/).
{{% /blocks/feature %}}

{{% blocks/feature icon="fa-solid fa-expand" title="Extensible and Customizable" %}}
Jikkou can be extended to manage almost anything. It provides a simple and powerful Core API (in Java) allowing you
to write custom extensions for managing your own system and resources.
{{% /blocks/feature %}}

{{% blocks/feature icon="fas fa-feather" title="Open source" %}}
Jikkou is released under the **Apache License 2.0**.
Anyone can contribute to Jikkou by opening an issue, a pull request (PR) or just by discussing with
other users on the [Slack Channel](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA).
{{% /blocks/feature %}}
{{% /blocks/section %}}

{{% blocks/section type="row" color="dark" %}}
{{% blocks/feature icon="fab fa-slack" title="Join us on Slack" %}}
Join the Jikkou community on Slack

<a class="text-white" href="https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VAy">
<button type="button" class="btn btn-github" style="width:150px; margin-top: 12px;">Joins Us</button>
</a>
{{% /blocks/feature %}}

{{% blocks/feature icon="fab fa-github" title="Contributions welcome" %}}
Want to join the fun on Github? New users are always welcome!

<a class="text-white" href="docs/contribution-guidelines/">
	<button type="button" class="btn btn-github" style="width:150px; margin-top: 12px;">Contribute</button>
</a>
{{% /blocks/feature %}}

{{% blocks/feature icon="fas fa-star" title="Support Jikkou Team" %}}
Add a star to the GitHub project, it only takes 5 seconds!

<a class="text-white" href="https://github.com/streamthoughts/jikkou">
	<button type="button" class="btn btn-github" style="width:150px; margin-top: 12px;">Star</button>
</a>
{{% /blocks/feature %}}
{{% /blocks/section %}}
