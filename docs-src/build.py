#!/usr/bin/env python3
"""Builds the documentation site in ../docs from the Markdown pages in ./pages.

    pip install markdown
    python3 docs-src/build.py

Each page starts with a small header (title, slug, nav) between --- lines.
"""
import html
import os
import re
import shutil

import markdown

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, '..', 'docs')
REPO = 'https://github.com/star4droid/libgdx-compat'
VERSION = '1.0.0'


def read_pages():
    pages = []
    for name in sorted(os.listdir(os.path.join(ROOT, 'pages'))):
        if not name.endswith('.md'):
            continue
        text = open(os.path.join(ROOT, 'pages', name), encoding='utf-8').read()
        m = re.match(r'---\n(.*?)\n---\n', text, re.S)
        meta = dict(line.split(':', 1) for line in m.group(1).splitlines())
        meta = {k.strip(): v.strip() for k, v in meta.items()}
        meta['body'] = text[m.end():]
        meta['source'] = name
        pages.append(meta)
    return pages


def render(md_text):
    md = markdown.Markdown(extensions=['tables', 'fenced_code', 'toc', 'attr_list'],
                           extension_configs={'toc': {'permalink': '#', 'permalink_class': 'anchor', 'toc_depth': '2-3'}})
    body = md.convert(md_text)
    toc = [(t['id'], t['name']) for t in md.toc_tokens for t in [t]]  # level-2 headings (h1 is the page title)
    flat = []
    for t in md.toc_tokens:
        if t['level'] == 1:
            flat += [(c['id'], c['name']) for c in t['children']]
        elif t['level'] == 2:
            flat.append((t['id'], t['name']))
    # fenced code: <pre><code class="language-java"> -> add a wrapper for the copy button
    body = re.sub(r'<pre><code class="language-(\w+)">', r'<pre class="code" data-lang="\1"><code class="language-\1">', body)
    body = body.replace('<table>', '<div class="table-wrap"><table>').replace('</table>', '</table></div>')
    return body, flat


TEMPLATE = '''<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title} · libgdx-compat</title>
<meta name="description" content="libgdx-compat: AppCompat-style Material 3 UI components for libGDX. {title}.">
<link rel="icon" href="images/favicon.svg" type="image/svg+xml">
<link rel="stylesheet" href="assets/style.css">
<script>try{{var t=localStorage.getItem('theme');if(t)document.documentElement.dataset.theme=t;}}catch(e){{}}</script>
</head>
<body>
<header class="topbar">
  <button class="menu-btn" aria-label="Menu" onclick="document.body.classList.toggle('nav-open')">&#9776;</button>
  <a class="brand" href="index.html"><img src="images/favicon.svg" alt=""> libgdx-compat <span class="version">v{version}</span></a>
  <span class="spacer"></span>
  <button class="theme-btn" aria-label="Toggle dark mode" title="Toggle dark mode"><svg viewBox="0 0 24 24" width="20" height="20"><path fill="currentColor" d="M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.4 5.4 0 0 1-4.4 2.26 5.4 5.4 0 0 1-3.14-9.8A8.9 8.9 0 0 0 12 3z"/></svg></button>
  <a class="gh" href="{repo}" aria-label="GitHub">GitHub</a>
</header>
<div class="layout">
  <nav class="sidebar">
{nav}
    <div class="side-links">
      <a href="{repo}">Source code</a>
      <a href="{repo}/releases">Releases &amp; APK</a>
      <a href="{repo}/issues">Report an issue</a>
    </div>
  </nav>
  <main class="content{main_class}">
{body}
    <div class="pager">{prev}{next}</div>
    <footer>libgdx-compat · <a href="{repo}/blob/main/docs-src/pages/{source}">Edit this page</a></footer>
  </main>
  <aside class="toc">{toc}</aside>
</div>
<div class="scrim" onclick="document.body.classList.remove('nav-open')"></div>
<script src="assets/site.js"></script>
</body>
</html>
'''


def main():
    pages = read_pages()
    os.makedirs(os.path.join(OUT, 'assets'), exist_ok=True)
    for f in os.listdir(os.path.join(ROOT, 'assets')):
        shutil.copy(os.path.join(ROOT, 'assets', f), os.path.join(OUT, 'assets', f))
    for i, p in enumerate(pages):
        body, toc = render(p['body'])
        nav = '\n'.join('    <a class="{}" href="{}.html">{}</a>'.format('active' if q is p else '', q['slug'], html.escape(q['nav']))
                        for q in pages)
        toc_html = ''
        if len(toc) > 1:
            toc_html = '<div class="toc-title">On this page</div>' + ''.join(
                '<a href="#{}">{}</a>'.format(a, n) for a, n in toc)
        prev = next_ = ''
        if i > 0:
            prev = '<a class="prev" href="{}.html"><span>Previous</span>{}</a>'.format(pages[i - 1]['slug'], html.escape(pages[i - 1]['nav']))
        if i < len(pages) - 1:
            next_ = '<a class="next" href="{}.html"><span>Next</span>{}</a>'.format(pages[i + 1]['slug'], html.escape(pages[i + 1]['nav']))
        out = TEMPLATE.format(title=html.escape(p['title']), version=VERSION, repo=REPO, nav=nav, body=body, toc=toc_html,
                              prev=prev, next=next_, source=p['source'], main_class=' home' if p['slug'] == 'index' else '')
        open(os.path.join(OUT, p['slug'] + '.html'), 'w', encoding='utf-8').write(out)
        print('built', p['slug'] + '.html')
    open(os.path.join(OUT, '.nojekyll'), 'w').close()


if __name__ == '__main__':
    main()
