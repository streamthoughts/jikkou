---
title: Jikkou
linkTitle: Jikkou
---

{{% blocks/cover image_anchor="top" height="max" color="white" %}}
<div class="row align-items-center hero-layout">
    <div class="col-lg-5 hero-left">
        <div class="hero-content">
            <div class="hero-badge">
                <span class="hero-badge-dot"></span>
                Open Source &middot; Apache License 2.0
            </div>
            <h1 class="hero-title">Resource as Code<br/>for <span class="hero-gradient-text">Apache Kafka</span><span class="hero-reg">&reg;</span></h1>
            <p class="hero-subtitle">
                Declare, automate, and provision Topics, ACLs, Schemas, Quotas, and Connectors for your entire Kafka platform — from simple YAML files.
            </p>
            <div class="hero-ctas">
                <a class="btn btn-primary-cta" href="docs/install">
                    Get Started <i class="fas fa-arrow-right ms-2"></i>
                </a>
                <a class="btn btn-ghost-cta" href="https://github.com/streamthoughts/jikkou">
                    <i class="fab fa-github me-2"></i> View on GitHub
                </a>
            </div>
            <div class="hero-stats">
                <div class="hero-stat">
                    <span class="hero-stat-value">5+</span>
                    <span class="hero-stat-label">Kafka Platforms</span>
                </div>
                <div class="hero-stat-divider"></div>
                <div class="hero-stat">
                    <span class="hero-stat-value">20+</span>
                    <span class="hero-stat-label">Resource Types</span>
                </div>
                <div class="hero-stat-divider"></div>
                <div class="hero-stat">
                    <span class="hero-stat-value">CLI + API</span>
                    <span class="hero-stat-label">Dual Interface</span>
                </div>
            </div>
        </div>
    </div>
    <div class="col-lg-7 hero-right">
        <div class="hero-code-float">
            <div class="code-window">
                <div class="code-window-header">
                    <div class="code-dots">
                        <span class="code-dot red"></span>
                        <span class="code-dot yellow"></span>
                        <span class="code-dot green"></span>
                    </div>
                    <span class="code-filename">kafka-topic.yaml</span>
                    <span class="code-badge">YAML</span>
                </div>
                <div class="code-window-body">
<pre><code><span class="c-key">apiVersion</span><span class="c-colon">:</span> <span class="c-val">kafka.jikkou.io/v1</span>
<span class="c-key">kind</span><span class="c-colon">:</span> <span class="c-val">KafkaTopic</span>
<span class="c-key">metadata</span><span class="c-colon">:</span>
  <span class="c-key">name</span><span class="c-colon">:</span> <span class="c-str">'my-topic'</span>
<span class="c-key">spec</span><span class="c-colon">:</span>
  <span class="c-key">partitions</span><span class="c-colon">:</span> <span class="c-num">6</span>
  <span class="c-key">replicas</span><span class="c-colon">:</span> <span class="c-num">3</span>
  <span class="c-key">configs</span><span class="c-colon">:</span>
    <span class="c-key">retention.ms</span><span class="c-colon">:</span> <span class="c-num">604800000</span>
    <span class="c-key">cleanup.policy</span><span class="c-colon">:</span> <span class="c-str">'compact'</span></code></pre>
                </div>
            </div>
            <div class="code-window code-window--cli">
                <div class="code-window-header">
                    <div class="code-dots">
                        <span class="code-dot red"></span>
                        <span class="code-dot yellow"></span>
                        <span class="code-dot green"></span>
                    </div>
                    <span class="code-filename">Terminal</span>
                </div>
                <div class="code-window-body">
<pre><code><span class="c-prompt">$</span> <span class="c-cmd">jikkou</span> apply --files kafka-topic.yaml
<span class="c-ok">&#10003;</span> <span class="c-dim">Validating resources...</span>
<span class="c-ok">&#10003;</span> <span class="c-dim">Computing changes...</span>
<span class="c-ok">&#10003;</span> <span class="c-dim">Applying 1 change(s)</span>

<span class="c-ok">CHANGED</span>  KafkaTopic/<span class="c-val">my-topic</span> <span class="c-dim">(partitions: 6, replicas: 3)</span>

<span class="c-ok">&#10003; Reconciliation completed.</span> <span class="c-num">1</span> <span class="c-dim">changed,</span> <span class="c-num">0</span> <span class="c-dim">failed</span></code></pre>
                </div>
            </div>
        </div>
    </div>
</div>
{{% /blocks/cover %}}

<!-- Integrations Strip -->
<section class="integrations-strip">
    <div class="container">
        <p class="integrations-label">Works with your favorite Apache Kafka platforms</p>
        <div class="integrations-grid">
            <div class="integration-item">
                <i class="fas fa-stream"></i>
                <span>Apache Kafka</span>
            </div>
            <div class="integration-item">
                <i class="fas fa-cloud"></i>
                <span>Confluent Cloud</span>
            </div>
            <div class="integration-item">
                <i class="fas fa-server"></i>
                <span>Amazon MSK</span>
            </div>
            <div class="integration-item">
                <i class="fas fa-database"></i>
                <span>Aiven</span>
            </div>
            <div class="integration-item">
                <i class="fas fa-bolt"></i>
                <span>Redpanda</span>
            </div>
        </div>
    </div>
</section>

<!-- What is Jikkou - Showcase -->
{{% blocks/showcase color="white" %}}
{{% asciinema key="demo" autoPlay="true" loop="true" fit="none" rows="45" cols="200" terminalFontSize="15px" terminalLineHeight="1.1" theme="solarized-dark" %}}
{{% /blocks/showcase %}}

<!-- Why Jikkou -->
<section class="why-section">
    <div class="container">
        <div class="section-header">
            <span class="section-badge">Features</span>
            <h2 class="section-heading">Why teams choose Jikkou</h2>
            <p class="section-subheading">A single framework to manage your entire Kafka ecosystem with confidence</p>
        </div>
        <div class="features-grid">
            <div class="feature-card">
                <div class="feature-icon-wrapper">
                    <i class="fas fa-file-code"></i>
                </div>
                <h3>Declarative & Automated</h3>
                <p>Describe the desired state of every resource using YAML. Jikkou computes the diff and applies only what changed — no manual intervention needed.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon-wrapper feature-icon--pink">
                    <i class="fas fa-plug"></i>
                </div>
                <h3>Multi-Platform Support</h3>
                <p>Built for Apache Kafka and compatible with <strong>Confluent Cloud</strong>, <strong>Aiven</strong>, <strong>Amazon MSK</strong>, <strong>Redpanda</strong>, and <strong>Schema Registry</strong>.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon-wrapper feature-icon--cyan">
                    <i class="fas fa-puzzle-piece"></i>
                </div>
                <h3>Extensible Architecture</h3>
                <p>Extend Jikkou with custom providers, transformations, and validations using a simple Java API. Manage any resource, not just Kafka.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon-wrapper feature-icon--amber">
                    <i class="fas fa-terminal"></i>
                </div>
                <h3>CLI & API Server</h3>
                <p>Use the powerful CLI for local workflows and CI/CD, or deploy the REST API server for team-wide, centralized management.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon-wrapper feature-icon--emerald">
                    <i class="fas fa-code-branch"></i>
                </div>
                <h3>GitOps Ready</h3>
                <p>Version-control your Kafka resources alongside application code. Review changes through pull requests and automate with CI/CD pipelines.</p>
            </div>
            <div class="feature-card">
                <div class="feature-icon-wrapper feature-icon--indigo">
                    <i class="fas fa-shield-alt"></i>
                </div>
                <h3>Safe by Design</h3>
                <p>Built-in dry-run mode, validations, and transformations ensure changes are safe before applying to production clusters.</p>
            </div>
        </div>
    </div>
</section>

<!-- How it works -->
<section class="how-it-works-section">
    <div class="container">
        <div class="section-header">
            <span class="section-badge section-badge--dark">Workflow</span>
            <h2 class="section-heading section-heading--light">How it works</h2>
            <p class="section-subheading section-subheading--light">Three simple steps to manage your Kafka resources</p>
        </div>
        <div class="steps-grid">
            <div class="step-card">
                <div class="step-number">1</div>
                <h3>Describe</h3>
                <p>Define your Kafka resources — topics, ACLs, schemas, quotas — in declarative YAML files.</p>
            </div>
            <div class="step-connector">
                <svg width="40" height="2"><line x1="0" y1="1" x2="40" y2="1" stroke="rgba(255,255,255,0.2)" stroke-width="2" stroke-dasharray="4 4"/></svg>
            </div>
            <div class="step-card">
                <div class="step-number">2</div>
                <h3>Validate</h3>
                <p>Run <code>jikkou diff</code> to preview changes. Built-in validations catch errors before they reach production.</p>
            </div>
            <div class="step-connector">
                <svg width="40" height="2"><line x1="0" y1="1" x2="40" y2="1" stroke="rgba(255,255,255,0.2)" stroke-width="2" stroke-dasharray="4 4"/></svg>
            </div>
            <div class="step-card">
                <div class="step-number">3</div>
                <h3>Apply</h3>
                <p>Execute <code>jikkou apply</code> to reconcile the desired state. Only the necessary changes are applied.</p>
            </div>
        </div>
    </div>
</section>

<!-- Community & Open Source -->
<section class="community-section">
    <div class="container">
        <div class="section-header">
            <span class="section-badge">Community</span>
            <h2 class="section-heading">Open Source & Community Driven</h2>
            <p class="section-subheading">Jikkou is built in the open. Join the community and help shape the future of Kafka resource management.</p>
        </div>
        <div class="community-grid">
            <a class="community-card" href="https://github.com/streamthoughts/jikkou">
                <div class="community-icon"><i class="fab fa-github"></i></div>
                <h3>Star on GitHub</h3>
                <p>Browse the source, open issues, and contribute</p>
                <span class="community-link">github.com/streamthoughts/jikkou <i class="fas fa-arrow-right"></i></span>
            </a>
            <a class="community-card" href="https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA">
                <div class="community-icon community-icon--pink"><i class="fab fa-slack"></i></div>
                <h3>Join Slack</h3>
                <p>Chat with maintainers and other users</p>
                <span class="community-link">jikkou-io.slack.com <i class="fas fa-arrow-right"></i></span>
            </a>
            <a class="community-card" href="docs/contribution-guidelines/">
                <div class="community-icon community-icon--cyan"><i class="fas fa-heart"></i></div>
                <h3>Contribute</h3>
                <p>Submit pull requests and help improve Jikkou</p>
                <span class="community-link">Contribution guidelines <i class="fas fa-arrow-right"></i></span>
            </a>
        </div>
    </div>
</section>
