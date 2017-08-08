#!/usr/bin/env runhaskell

{-# LANGUAGE FlexibleContexts #-}
{-# LANGUAGE TemplateHaskell #-}
{-# LANGUAGE RankNTypes #-}

module Main where

import           Control.Arrow
import           Control.Lens
import           Control.Monad
import           Control.Monad.IO.Class

import qualified Data.ByteString.Char8 as Char8
import qualified Data.IORef as IORef
import qualified Data.List as List
import qualified Data.List.Utils as List.Utils
import qualified Data.Map.Strict as Map
import qualified Data.Maybe as Maybe
import qualified Data.Set as Set
import           Data.Tagged
import qualified Data.Text as Text

import qualified Git.Libgit2
import qualified Git.Reference
import qualified Git.Repository
import qualified Git.Types
import qualified Git.Tree

import qualified System.Environment as Environment

import           Wbs.Config

---------- data types

type TreeEntries r =
	[(Char8.ByteString, Git.Types.TreeEntry r)]

type CacheRef r =
	IORef.IORef (Cache r)

type Tree r =
	Git.Types.Tree r

type TreeId r =
	Git.Types.TreeOid r

type Commit r =
	Git.Types.Commit r

type CommitId r =
	Git.Types.CommitOid r

type CommitSet r =
	Map.Map (CommitId r) (Commit r)

data Cache r = Cache {

	_cacheCommits ::
		Map.Map
		(CommitId r)
		(Commit r),

	_cacheTrees ::
		Map.Map
		(TreeId r)
		(Tree r),

	_cacheBuiltTrees ::
		Map.Map
		[(Char8.ByteString, Git.Types.Oid r)]
		(Tree r),

	_cacheParents ::
		Map.Map
		(CommitId r)
		[Commit r]

}

makeLenses ''Cache

---------- tree stuff

treeId ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	Tree r ->
	m (TreeId r)

treeId tree =
	Git.Types.treeOid tree

---------- commit stuff

-- commitId - returns the commit id for a commit

commitId ::
	Ord (CommitId r) =>
	Commit r ->
	CommitId r

commitId commit =
	Git.Types.commitOid commit

-- commitTree - returns the tree for a commit

commitTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	Commit r ->
	m (Tree r)

commitTree cacheRef commit = do

	let treeIdTemp =
		Git.Types.commitTree commit

	loadTree cacheRef treeIdTemp

-- commitSetMember - tests if a commit is in a set

commitSetMember ::
	Ord (CommitId r) =>
	Commit r ->
	CommitSet r ->
	Bool

commitSetMember commit set =
	Map.member (commitId commit) set

-- commitSetEmpty - returns an empty commit set

commitSetEmpty ::
	CommitSet r

commitSetEmpty =
	Map.empty

-- commitSetElems - get commits in commit set

commitSetElems set =
	Map.elems set

-- commitSetInsert - insert a commit in a commit set

commitSetInsert ::
	Ord (CommitId r) =>
	Commit r ->
	CommitSet r ->
	CommitSet r

commitSetInsert commit set =
	Map.insert (commitId commit) commit set

-- commitSetFromList - create a commit set given a list

commitSetFromList ::
	Ord (CommitId r) =>
	[Commit r] ->
	CommitSet r

commitSetFromList =
	foldl (\set commit -> commitSetInsert commit set) commitSetEmpty

-- commitSetUnion -

commitSetUnion ::
	Ord (CommitId r) =>
	CommitSet r ->
	CommitSet r ->
	CommitSet r

commitSetUnion left right =
	Map.union left right

---------- cache

createCache ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	m (CacheRef r)

createCache = do

	liftIO $ IORef.newIORef Cache {
		_cacheCommits = Map.empty,
		_cacheTrees = Map.empty,
		_cacheBuiltTrees = Map.empty,
		_cacheParents = Map.empty }

loadCached ::
	(Git.Types.MonadGit r m, MonadIO m, Ord k') =>
	Lens' (Cache r) (Map.Map k' a) ->
	(k -> k') ->
	(k -> m a) ->
	IORef.IORef (Cache r) ->
	k ->
	m (a)

loadCached lens mungeKey lookup cacheRef id = do

	cache <-
		liftIO $ IORef.readIORef cacheRef

	let map =
		view lens cache

	let maybeValue =
		Map.lookup (mungeKey id) map

	case maybeValue of

		Just value -> do

			return value

		Nothing -> do

			value <-
				lookup id

			let map' =
				Map.insert (mungeKey id) value map

			let cache' =
				set lens map' cache

			liftIO $ IORef.writeIORef cacheRef cache'

			return value

loadTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	Git.Types.TreeOid r ->
	m (Git.Types.Tree r)

loadTree cacheRef treeOid =

	loadCached
		cacheTrees
		id
		Git.Types.lookupTree
		cacheRef
		treeOid

loadCommit ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	Git.Types.CommitOid r ->
	m (Git.Types.Commit r)

loadCommit cacheRef commitOid =

	loadCached
		cacheCommits
		id
		Git.Types.lookupCommit
		cacheRef
		commitOid

lookupBuiltTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	TreeEntries r ->
	m (Tree r)

lookupBuiltTree cacheRef entries =

	loadCached
		cacheBuiltTrees
		(map $ second Git.Types.treeEntryToOid)
		(addEntriesToTree cacheRef Nothing)
		cacheRef
		entries

commitParents ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	Commit r ->
	m ([Commit r])

commitParents cacheRef commit =

	loadCached
		cacheParents
		commitId
		(\commit -> mapM (loadCommit cacheRef) $ Git.Types.commitParents commit)
		cacheRef
		commit

---------- tree building

addEntriesToTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	Maybe (Git.Types.Tree r) ->
	TreeEntries r ->
	m (Git.Types.Tree r)

addEntriesToTree cacheRef originalTreeMaybe entries = do

	let pathSplit path =
		List.Utils.split "/" (Char8.unpack path)

	treeBuilder <-
		Git.Types.newTreeBuilder originalTreeMaybe

	let entriesTemp =
		foldl addEntry Map.empty entries

		where addEntry map (path, entry) = let

			pathParts =
				pathSplit path

			firstPart =
				head pathParts

			remainingParts =
				tail pathParts

			remainingPathString =
				List.intercalate "/" remainingParts

			remainingPath =
				Char8.pack remainingPathString

			in case (length pathParts) of

				1 ->
					Map.insert firstPart (Left (path, entry)) map

				_ ->
					Map.alter mergeEntries firstPart map where

						mergeEntries Nothing =
							Just $ Right $
								[ (remainingPath, entry) ]

						mergeEntries (Just (Right list)) =
							Just $ Right $
								((remainingPath, entry) : list)

	let

		makeSubEntry (_path, Left (path, entry)) = do
			return (path, entry)

		makeSubEntry (path, Right entries) = do

			existingSubTreeEntryMaybe <-
				Git.Types.mtbLookupEntry
					treeBuilder
					(Char8.pack path)

			subTree <-
				case existingSubTreeEntryMaybe of

				Just existingSubTreeEntry -> do

					let existingSubTreeOid =
						Tagged $ Git.Types.treeEntryToOid existingSubTreeEntry

					existingSubTree <-
						loadTree cacheRef existingSubTreeOid

					addEntriesToTree
						cacheRef
						(Just existingSubTree)
						entries

				Nothing -> do

					lookupBuiltTree
						cacheRef
						entries

			subTreeOid <-
				Git.Types.treeOid subTree

			let subTreeEntry =
				Git.Types.TreeEntry {
					Git.Types.treeEntryOid = subTreeOid }

			return ((Char8.pack path), subTreeEntry)

	newEntries <-
		mapM makeSubEntry (Map.toAscList entriesTemp)

	let addEntry treeBuilder (path, entry) = do

		modifiedTree <-
			Git.Types.mtbPutEntry treeBuilder treeBuilder path entry

		return $
			Git.Types.fromBuilderMod modifiedTree

	modifiedBuilder <-
		foldM addEntry treeBuilder newEntries

	(_, newTreeOid) <-
		Git.Types.mtbWriteContents modifiedBuilder modifiedBuilder

	newTree <-
		loadTree cacheRef newTreeOid

	return newTree

---------- recursive tree lookup

treeLookup ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	Git.Types.Tree r ->
	Char8.ByteString ->
	m (Maybe (Git.Types.TreeEntry r))

treeLookup cacheRef tree path = do

	treeBuilder <-
		Git.Types.newTreeBuilder (Just tree)

	let pathParts =
		List.Utils.split "/" (Char8.unpack path)

	case pathParts of

		[ singlePathPart ] ->

			Git.Types.mtbLookupEntry
				treeBuilder
				path

		(firstPathPart : restOfPathParts) -> do

			maybeSubTreeEntry <-
				Git.Types.mtbLookupEntry treeBuilder (Char8.pack firstPathPart)

			case maybeSubTreeEntry of

				Nothing ->
					return Nothing

				_ -> do

					let Just subTreeEntry =
						maybeSubTreeEntry

					let subTreeOid =
						Git.Types.treeEntryToOid subTreeEntry

					subTree <-
						loadTree cacheRef (Tagged subTreeOid)

					let restOfPath =
						Char8.pack $
							List.intercalate "/" restOfPathParts

					treeLookup cacheRef subTree restOfPath

---------- reduction

filterTreeToEntries ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	[Char8.ByteString] ->
	Git.Types.Tree r ->
	m (TreeEntries r)

filterTreeToEntries cacheRef paths tree = do

	treeBuilder <-
		Git.Types.newTreeBuilder (Just tree)

	let lookupOne path = do

		treeEntryMaybe <-
			treeLookup cacheRef tree path

		case treeEntryMaybe of

			Nothing ->
				return Nothing

			Just treeEntry ->
				return $ Just (path, treeEntry)

	filteredTreeEntriesTemp <-
		mapM lookupOne paths

	let filteredTreeEntries =
		Maybe.catMaybes filteredTreeEntriesTemp

	return filteredTreeEntries

filterTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	[Char8.ByteString] ->
	Git.Types.Tree r ->
	m (Git.Types.Tree r)

filterTree cacheRef paths tree = do

	filteredTreeEntries <-
		filterTreeToEntries cacheRef paths tree

	lookupBuiltTree cacheRef filteredTreeEntries

---------- fast forwards

commitAndAncestors' ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	CommitSet r ->
	Commit r ->
	m (CommitSet r)

commitAndAncestors' cacheRef set commit = do

	if commitSetMember commit set

	then do

		return set

	else do

		let set' =
			commitSetInsert commit set

		parents <-
			commitParents cacheRef commit

		foldM (commitAndAncestors' cacheRef) set' parents

commitAndAncestors ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	Commit r ->
	m (CommitSet r)

commitAndAncestors cacheRef commit =
	commitAndAncestors' cacheRef commitSetEmpty commit

eliminateFastForwards ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	[Commit r] ->
	m ([Commit r])

eliminateFastForwards cacheRef [] =
	return []

eliminateFastForwards cacheRef [ oneCommit ] =
	return [ oneCommit ]

eliminateFastForwards cacheRef commits = do

	listOfParentLists <-
		mapM (commitParents cacheRef) commits

	let listOfParentSets =
		map commitSetFromList listOfParentLists

	let combinedParents =
		foldl commitSetUnion commitSetEmpty listOfParentSets

	allAncestors <-
		foldM
			(commitAndAncestors' cacheRef)
			commitSetEmpty
			(commitSetElems combinedParents)

	let shouldEliminate commit =
		commitSetMember commit allAncestors

	return $ filter (not . shouldEliminate) commits

type ExpansionsMap r =
	Map.Map
	(Git.Types.CommitOid r)
	(Git.Types.Commit r)

reduceCommit ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	[Char8.ByteString] ->
	ExpansionsMap r ->
	Commit r ->
	m (Commit r, ExpansionsMap r)

reduceCommit cacheRef paths expansions commit = do

	-- reduce parents

	commitParentsTemp <-
		commitParents cacheRef commit

	reducedTemp <-
		mapM (reduceCommit cacheRef paths expansions)
			commitParentsTemp

	let reducedParents =
		map fst reducedTemp

	let parentExpansions =
		map snd reducedTemp

	let expansions' =
		foldl Map.union expansions parentExpansions

	-- eliminate fast-forwards

	validParents <-
		eliminateFastForwards cacheRef reducedParents

	-- filter tree

	originalTree <-
		commitTree cacheRef commit

	filteredTree <-
		filterTree cacheRef paths originalTree

	-- drop commits with no changes

	dropCommitMaybe <- do

		case validParents of

			[ singleParent ] -> do

				parentTree <-
					commitTree cacheRef singleParent

				parentTreeId <-
					treeId parentTree

				filteredTreeId <-
					treeId filteredTree

				if parentTreeId == filteredTreeId
				then return $ Just singleParent
				else return Nothing

			_ ->

				return Nothing

	case dropCommitMaybe of

		Just dropCommit -> do

			return (dropCommit, expansions')

		Nothing -> do

			filteredTreeId <-
				treeId filteredTree

			reduced <-
				Git.Types.createCommit
					(map commitId validParents)
					filteredTreeId
					(Git.Types.commitAuthor commit)
					(Git.Types.commitCommitter commit)
					(Git.Types.commitLog commit)
					Nothing

			let expansions'' =
				if (commitId reduced) == (commitId commit)
				then expansions'
				else Map.insert
					(commitId reduced)
					commit
					expansions'

			return (reduced, expansions'')

---------- rewriting

rewriteCommit cacheRef paths expansions originalCommit = do

	case Map.lookup (commitId originalCommit) expansions of

		Just expandedCommit ->
			return expandedCommit

		Nothing -> do

			originalParentCommits <-
				commitParents cacheRef originalCommit

			rewrittenParentCommits <-
				mapM (rewriteCommit cacheRef paths expansions)
					originalParentCommits

			case rewrittenParentCommits of

				[] ->

					return originalCommit

				[ rewrittenParentCommit ] -> do

					rewrittenParentTree <-
						commitTree cacheRef rewrittenParentCommit

					originalTree <-
						commitTree cacheRef originalCommit

					filteredEntries <-
						filterTreeToEntries cacheRef paths originalTree

					rewrittenTree <-
						addEntriesToTree
							cacheRef
							(Just rewrittenParentTree)
							filteredEntries

					rewrittenTreeOid <-
						Git.Types.treeOid rewrittenTree

					rewrittenCommit <-
						Git.Types.createCommit
							(map commitId rewrittenParentCommits)
							rewrittenTreeOid
							(Git.Types.commitAuthor originalCommit)
							(Git.Types.commitCommitter originalCommit)
							(Git.Types.commitLog originalCommit)
							Nothing

					return rewrittenCommit

---------- main

withGit path =
	Git.Repository.withRepository
		Git.Libgit2.lgFactory
		path

performReduction cacheRef paths referenceName = do

	originalCommitOidUntaggedMaybe <-
		Git.Reference.resolveReference $
			Text.pack referenceName

	case originalCommitOidUntaggedMaybe of

		Nothing ->
			error $
				"error resolving reference " ++
				referenceName

		_ ->
			return ()

	let Just originalCommitOidUntagged =
		originalCommitOidUntaggedMaybe

	let originalCommitOid =
		Tagged originalCommitOidUntagged

	originalCommit <-
		loadCommit cacheRef originalCommitOid

	(reducedCommit, expansions) <-
		reduceCommit cacheRef paths Map.empty originalCommit

	return (originalCommit, reducedCommit, expansions)

main :: IO ()
main = do

	buildConfig <- loadBuild
	arguments <- Environment.getArgs

	let [ gitLinkNameArg ] = arguments

	let Just gitLink =
		List.find
			(\gitLink -> (bglName gitLink) == gitLinkNameArg)
			(bldGitLinks buildConfig)

	withGit "." $ do

		cacheRef <- createCache

		let paths = map Char8.pack $ bglPaths gitLink

		-- reduce local commit

		(originalLocalCommit, reducedLocalCommit, expansions) <-
			performReduction
				cacheRef
				paths
				(bglLocal gitLink)

		let originalLocalCommitOid =
			Git.Types.commitOid originalLocalCommit

		let reducedLocalCommitOid =
			Git.Types.commitOid reducedLocalCommit

		liftIO $ putStrLn $
			"reduced local commit " ++
			(show $ untag originalLocalCommitOid) ++ " " ++
			"to " ++
			(show $ untag reducedLocalCommitOid)

		-- reduce source commit

		(originalSourceCommit, reducedSourceCommit, _) <-
			performReduction
				cacheRef
				paths
				(bglSource gitLink)

		let originalSourceCommitOid =
			Git.Types.commitOid originalSourceCommit

		let reducedSourceCommitOid =
			Git.Types.commitOid reducedSourceCommit

		liftIO $ putStrLn $
			"reduced source commit " ++
			(show $ untag originalSourceCommitOid) ++ " " ++
			"to " ++
			(show $ untag reducedSourceCommitOid)

		-- rewrite source to produce target

		targetCommit <-
			rewriteCommit
				cacheRef
				paths
				expansions
				reducedSourceCommit

		let targetCommitOid =
			Git.Types.commitOid targetCommit

		Git.Types.updateReference
			(Text.pack $ bglTarget gitLink)
			(Git.Types.RefObj (untag targetCommitOid))

		liftIO $ putStrLn $
			"rewritten to " ++
			(show $ untag targetCommitOid) ++ " " ++
			"and saved as " ++
			(bglTarget gitLink)

		return ()
