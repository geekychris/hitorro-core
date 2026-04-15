/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

import com.hitorro.util.log.Logger;

public class Log {
    public static Logger util = Logger.getLogger("ht.util");
    public static Logger servicecontext = Logger.getLogger("ht.servicecontext");
    public static Logger ssh = Logger.getLogger("ht.ssh");
    public static Logger exec = Logger.getLogger("com.hitorro.util.exec");
    public static Logger commands = Logger.getLogger("ht.commands");
    public static Logger hibernate = Logger.getLogger("ht.hibernate");
    public static Logger dmssession = Logger.getLogger("ht.dmssession");
    public static Logger test = Logger.getLogger("ht.test");
    public static Logger rpc = Logger.getLogger("ht.rpc");
    public static Logger resourcecache = Logger.getLogger("ht.resourcecache");
    public static Logger spam = Logger.getLogger("ht.spam");
    public static Logger queue = Logger.getLogger("ht.queue");
    public static Logger httpfetcher = Logger.getLogger("ht.htmlfetcher");
    public static Logger jdbc = Logger.getLogger("ht.jdbc");
    public static Logger breadcrumb = Logger.getLogger("ht.breadcrumb");
    public static Logger dbms = Logger.getLogger("ht.db");
    public static Logger mail = Logger.getLogger("ht.mail");
    public static Logger audit = Logger.getLogger("ht.audit");
    public static Logger type = Logger.getLogger("ht.type");
    public static Logger coordination = Logger.getLogger("ht.coordination");


    public static Logger indexer = Logger.getLogger("ht.indexer");


    public static Logger workflow = Logger.getLogger("ht.workflow");
    public static Logger statemachine = Logger.getLogger("ht.statemachine");
    public static Logger streamer = Logger.getLogger("ht.streamer");

    public static Logger filesystem = Logger.getLogger("ht.filesystem");

    /**
     * Logger for scheduled jobs.
     */
    public static Logger scheduledJobs = Logger.getLogger("ht.scheduledJobs");

    public static Logger extractors = Logger.getLogger("ht.extractors");
    public static Logger sentence = Logger.getLogger("ht.sentence");

    public static Logger mstparser = Logger.getLogger("ht.mstparser");

    public static Logger unitTime = Logger.getLogger("ht.unittime");

    public static Logger docprocessor = Logger.getLogger("ht.docprocessor");
    public static Logger docmap = Logger.getLogger("ht.docmap");
    public static Logger idmanager = Logger.getLogger("ht.persistedbloomfilter");
    public static Logger decisiontrees = Logger.getLogger("ht.decisiontrees");
    public static Logger ftpfs = Logger.getLogger("ht.ftpfs");
    public static Logger jmx = Logger.getLogger("ht.jmx");

    public static Logger fetcher = Logger.getLogger("ht.fetcher");

    public static Logger io = Logger.getLogger("ht.io");
    public static Logger iterator = Logger.getLogger("ht.iterator");

    public static Logger importer = Logger.getLogger("ht.importer");

    public static Logger compression = Logger.getLogger("ht.compression");
    public static Logger tapestry = Logger.getLogger("ht.tapestry");
    public static Logger lang = Logger.getLogger("ht.lang");
    public static Logger json = Logger.getLogger("ht.json");

    public static Logger mongo = Logger.getLogger("ht.mongo");

    public static Logger tld = Logger.getLogger("ht.tld");

    public static Logger text = Logger.getLogger("ht.text");
}
