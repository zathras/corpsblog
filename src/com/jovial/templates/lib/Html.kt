package com.jovial.templates.lib

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by w.foote on 11/3/2016.
 *
 * This set of definitions creates a grammar for "groovy-like builders" for HTML.
 * Basically, you write Kotlin code that evaluates to a string containing HTML.
 *
 * Initial version copied from com.example.html at
 * http://kotlinlang.org/docs/reference/type-safe-builders.html
 *
 * As of this writing (Oct. 2019), this grammar is too permissive.
 * It allows certain tags to be nested that are invalid.  For example,
 * a heading tag can be nested inside another heading tag.  Getting this
 * right would be a fair amount of detail-oriented work; this felt like
 * overkill for a blog generator where I was playing around with the
 * builder pattern.
 */

private val yymmddDateFormat = SimpleDateFormat("yyyy-MM-dd")

@DslMarker annotation class HtmlTagMarker

/**
 * Something that can be the parent of a tag.  This is used to assert that
 * the builder methods don't get called from an outer scope -- see
 * Tag.initTag.  With the introduction of DslMarker in Kotlin 1.1,
 * this assertion is redundant, but it doesn't hurt.
 */
@HtmlTagMarker
interface TagParent {

    fun startInitAssert(depth: Int) : Boolean
    fun endInitAssert() : Boolean
}

interface Element {

    fun render(builder: StringBuilder, indent: String)
    fun tagName() : String
}

/**
 * An element that consists only of text, like the body of a paragraph.
 */
class TextElement(val text: String) : Element {
    override fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent$text\n")
    }
    override fun tagName() : String = "text element"
}

abstract class Tag(private val parent: TagParent, val name: String) : Element, TagParent {
    val children = arrayListOf<Element>()
    val attributes = arrayListOf<Pair<String, String>>()
        // key/value pairs, kept in order to avoid weird-looking HTML

    /**
     * Add an attribute if the value isn't null
     */
    fun addAttribute(k: String, v: String?) {
        if (v != null) {
            attributes += Pair(k, v)
        }
    }
    protected open fun isSelfClosing() : Boolean = false    // like <meta foo="bar"/>
    protected open fun noEndTag() : Boolean = false         // like <img src="foo.png">

    override fun startInitAssert(depth: Int) : Boolean {
        return parent.startInitAssert(depth+1)
    }

    override fun endInitAssert() : Boolean {
        return parent.endInitAssert()
    }

    protected fun <T : Element> initTag(tag: T, init: (T.() -> Unit)?): T {
        assert(startInitAssert(0)) {
            "Depth error in ${tag.tagName()} tag -- incorrect nesting of tag types"
            // It's tempting to define tag types at a higher level of the inheritance
            // hierarchy.  That's a Bad Idea, because of the multiple nesting of receiver
            // types.  For example, consider:
            //
            //  html {
            //    body {
            //      head { }  <--  Oops!  This calls html's head function
            //    }
            //  }
            //
            // This assertion catches that error, because head's parent is html, but
            // head's init is called recursively during the call to body's.
            //
            // In Kotlin 1.1, this problem can be solved, and made into a compile-time error using
            // @DslMarker.  See https://kotlinlang.org/docs/reference/whatsnew11.html and
            // https://github.com/Kotlin/KEEP/blob/master/proposals/scope-control-for-implicit-receivers.md

        }
        if (init != null) {
            tag.init()
        }
        children.add(tag)
        assert(endInitAssert())
        return tag
    }

    override fun render(builder: StringBuilder, indent: String) {
        if (isSelfClosing() && children.size == 0) {
            builder.append("$indent<$name${renderAttributes()}/>\n")
        } else {
            builder.append("$indent<$name${renderAttributes()}>\n")
            if (noEndTag()) {
                if (children.size > 0) {
                    throw RuntimeException("Internal error:  Children in a $name tag")
                }
            } else {
                for (c in children) {
                    c.render(builder, indent + "  ")
                }
                builder.append("$indent</$name>\n")
            }
        }

    }

    protected fun renderAttributes(): String? {
        val builder = StringBuilder()
        for (a in attributes) {
            builder.append(" ${a.first}=\"${a.second}\"")
        }
        return builder.toString()
    }


    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

    override fun tagName() : String = name
}

abstract class TagWithText(parent: TagParent, name: String) : Tag(parent, name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

abstract class HeadTag(parent: TagParent, name: String) : TagWithText(parent, name) {
    fun include(expr: HeadTag.() -> Unit) {
        expr()
    }

    fun meta(charset: String? = null,
             name: String? = null,
             author: String? = null,
             property: String? = null,
             content: String? = null)
    {
        val t = initTag(Meta(this), {})
        t.addAttribute("charset", charset)
        t.addAttribute("name", name)
        t.addAttribute("author", author)
        t.addAttribute("property", property)
        t.addAttribute("content", content)
    }
    fun link(href: String,
             rel: String,
             title: String? = null,
             type: String? = null)
    {
        val t = initTag(Link(this), {})
        t.attributes += Pair("href", href)
        t.attributes += Pair("rel", rel)
        t.addAttribute("title", title)
        t.addAttribute("type", type)
    }
    fun title(init: Title.() -> Unit) = initTag(Title(this), init)
    fun script(src: String? = null,
               type: String? = null,
               init: (ScriptInHead.() -> Unit)? = null)
    {
        val t = initTag(ScriptInHead(this), init)
        t.addAttribute("src", src)
        t.addAttribute("type", type)
    }
}

class Head(parent: TagParent) : HeadTag(parent, "head")
class Title(parent: TagParent) : HeadTag(parent, "title")
class Meta(parent: TagParent) : HeadTag(parent, "meta") {
    override protected fun isSelfClosing() : Boolean = true
}
class Link(parent: TagParent) : HeadTag(parent, "link") {
    override protected fun isSelfClosing() : Boolean = true
}

class ScriptInHead(parent: TagParent) : HeadTag(parent, "script")

abstract class BodyTag(parent: TagParent, name: String) : TagWithText(parent, name) {
    fun include(expr: BodyTag.() -> Unit) {
        expr()
    }
    fun script(src: String? = null,
               type: String? = null,
               init: (Script.() -> Unit)? = null)
    {
        val t = initTag(Script(this), init)
        t.addAttribute("src", src)
        t.addAttribute("type", type)
    }
    fun b(init: B.() -> Unit) = initTag(B(this), init)
    fun br(init: Br.() -> Unit = {}) = initTag(Br(this), init)
    fun em(init: Em.() -> Unit) = initTag(Em(this), init)
    fun p(style: String? = null,
          name: String? = null,
          content: String? = null,
          class_: String? = null,
          init: P.() -> Unit)
    {
        val t = initTag(P(this), init)
        t.addAttribute("style", style)
        t.addAttribute("name", name)
        t.addAttribute("content", content)
        t.addAttribute("class", class_)
    }
    fun blockquote(style: String? = null,
          name: String? = null,
          class_: String? = null,
          init: Blockquote.() -> Unit)
    {
        val t = initTag(Blockquote(this), init)
        t.addAttribute("style", style)
        t.addAttribute("name", name)
        t.addAttribute("class", class_)
    }
    fun div(class_: String? = null,
            id: String? = null,
            tabindex: String? = null,
            role: String? = null,
            aria_hidden: String? = null,
            style: String? = null,
            init: Div.() -> Unit) {
        val t = initTag(Div(this), init)
        t.addAttribute("id", id)
        t.addAttribute("class", class_)
        t.addAttribute("style", style)
        t.addAttribute("tabindex", tabindex)
        t.addAttribute("role", role)
        t.addAttribute("aria-hidden", aria_hidden)
    }
    fun nav(class_: String? = null,
            style: String? = null,
            init: Nav.() -> Unit)
    {
        val t = initTag(Nav(this), init)
        t.addAttribute("class", class_)
        t.addAttribute("style", style)
    }
    fun article(class_: String, init: Article.() -> Unit) {
        val t = initTag(Article(this), init)
        t.attributes += Pair("class", class_)
    }
    fun button(class_: String? = null, title: String? = null, onclick: String? = null, init: Button.() -> Unit) {
        val t = initTag(Button(this), init)
        t.addAttribute("class", class_)
        t.addAttribute("title", title)
        t.addAttribute("onclick", onclick)
    }
    fun section(class_: String, init: Section.() -> Unit) {
        val t = initTag(Section(this), init)
        t.attributes += Pair("class", class_)
    }
    fun header(class_: String, style: String? = null, init: Header.() -> Unit) {
        val t = initTag(Header(this), init)
        t.attributes += Pair("class", class_)
        t.addAttribute("style", style)
    }
    fun span(class_: String? = null, init: Span.() -> Unit) {
        val t = initTag(Span(this), init)
        t.addAttribute("class", class_)
    }
    fun i(class_: String? = null, init: I.() -> Unit) {
        val t = initTag(I(this), init)
        t.addAttribute("class", class_)
    }
    fun time(datetime: Date, class_: String? = null, init: Time.() -> Unit) {
        val t = initTag(Time(this), init)
        t.addAttribute("datetime", yymmddDateFormat.format(datetime))
        t.addAttribute("class", class_)
    }
    fun h1(class_: String? = null, init: H1.() -> Unit) {
        val t = initTag(H1(this), init)
        t.addAttribute("class", class_)
    }
    fun h2(class_: String? = null, init: H2.() -> Unit) {
        val t = initTag(H2(this), init)
        t.addAttribute("class", class_)
    }
    fun h3(class_: String? = null, init: H3.() -> Unit) {
        val t = initTag(H3(this), init)
        t.addAttribute("class", class_)
    }
    fun h4(class_: String? = null, init: H4.() -> Unit) {
        val t = initTag(H4(this), init)
        t.addAttribute("class", class_)
    }
    fun ul(class_: String? = null, init: Ul.() -> Unit) {
        val t = initTag(Ul(this), init)
        t.addAttribute("class", class_)
    }
    fun li(class_: String? = null, init: Li.() -> Unit) {
        val t = initTag(Li(this), init)
        t.addAttribute("class", class_)
    }
    fun a(href: String? = null,
          title: String? = null,
          target: String? = null,
          class_: String? = null,
          onclick: String? = null,
          style: String? = null,
          init: A.() -> Unit)
    {
        val t = initTag(A(this), init)
        t.addAttribute("href", href)
        t.addAttribute("title", title)
        t.addAttribute("target", target)
        t.addAttribute("class", class_)
        t.addAttribute("onclick", onclick)
        t.addAttribute("style", style)
    }
    fun img(src: String,
            alt: String? = null,
            class_: String? = null,
            style: String? = null,
            align: String? = null,
            width: String? = null,
            height: String? = null)
    {
        val t = initTag(Img(this), {})
        t.addAttribute("src", src)
        t.addAttribute("alt", alt ?: "*")
        t.addAttribute("class", class_)
        t.addAttribute("style", style)
        t.addAttribute("align", align)
        t.addAttribute("height", height)
        t.addAttribute("width", width)
    }
    fun hr(class_: String? = null) {
        val t = initTag(Hr(this), {})
        t.addAttribute("class", class_)
    }
    fun noscript(init: Noscript.() -> Unit) = initTag(Noscript(this), init)
    fun footer(class_: String, init: Footer.() -> Unit) {
        val t = initTag(Footer(this), init)
        t.attributes += Pair("class", class_)
    }
    //
    // This definition of table could be greatly improved to add static correctness checking.
    //
    fun table(init: Table.() -> Unit) = initTag(Table(this), init)
    fun tr(init: Tr.() -> Unit) = initTag(Tr(this), init)
    fun td(align: String? = null,
           valign: String? = null,
           init: Td.() -> Unit)
    {
        val t = initTag(Td(this), init)
        t.addAttribute("align", align)
        t.addAttribute("valign", valign)
    }
}

class Body(parent: TagParent) : BodyTag(parent, "body")
class Script(parent: TagParent) : BodyTag(parent, "script")
class B(parent: TagParent) : BodyTag(parent, "b")
class Br(parent: TagParent) : BodyTag(parent, "br") {
    override protected fun noEndTag(): Boolean = true
}
class Em(parent: TagParent) : BodyTag(parent, "em")
class P(parent: TagParent) : BodyTag(parent, "p")
class Blockquote(parent: TagParent) : BodyTag(parent, "p")
class H1(parent: TagParent) : BodyTag(parent, "h1")
class H2(parent: TagParent) : BodyTag(parent, "h2")
class H3(parent: TagParent) : BodyTag(parent, "h3")
class H4(parent: TagParent) : BodyTag(parent, "h4")
class Ul(parent: TagParent) : BodyTag(parent, "ul")
class Li(parent: TagParent) : BodyTag(parent, "li")

class Div(parent: TagParent) : BodyTag(parent, "div")
class Nav(parent: TagParent) : BodyTag(parent, "nav")

class Header(parent: TagParent) : BodyTag(parent, "header")
class Span(parent: TagParent) : BodyTag(parent, "span")
class I(parent: TagParent) : BodyTag(parent, "i")
class Time(parent: TagParent) : BodyTag(parent, "time")

class Article(parent: TagParent) : BodyTag(parent, "article")
class Button(parent: TagParent) : BodyTag(parent, "button")

class Section(parent: TagParent) : BodyTag(parent, "section")


class A(parent: TagParent) : BodyTag(parent, "a")
class Img(parent: TagParent) : BodyTag(parent, "img") {
    override protected fun noEndTag() : Boolean = true
}
class Hr(parent: TagParent) : BodyTag(parent, "hr") {
    override protected fun noEndTag() : Boolean = true
}
class Noscript(parent: TagParent) : BodyTag(parent, "noscript")
class Footer(parent: TagParent) : BodyTag(parent, "footer")
class Table(parent: TagParent) : BodyTag(parent, "table")
class Td(parent: TagParent) : BodyTag(parent, "td")
class Tr(parent: TagParent) : BodyTag(parent, "tr")


private class RootNode : TagParent {

    private var initDepth = 0

    override fun startInitAssert(depth: Int) : Boolean {
        initDepth++
        return initDepth == depth
    }

    override fun endInitAssert() : Boolean {
        initDepth--
        return true
    }
}

/**
 * A top-level html document, that is, an html tag.
 */
class HTML() : TagWithText(RootNode(), "html") {
    fun head(init: Head.() -> Unit) = initTag(Head(this), init)

    fun body(init: Body.() -> Unit) = initTag(Body(this), init)

    override fun render(builder: StringBuilder, indent: String) {
        builder.append("<!DOCTYPE html>\n")
        super.render(builder, indent)
    }
}


fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}

/**
 * An independent HTML fragment embedded within an HTML document.
 */
class BodyFragment : BodyTag(RootNode(), "") {
    override fun render(builder: StringBuilder, indent: String) {
        for (c in children) {
            c.render(builder, indent)
        }
    }
}

fun bodyFragment(init: BodyFragment.() -> Unit) : BodyFragment {
    val fragment = BodyFragment()
    fragment.init()
    return fragment
}
